package com.pedigreetechnologies.diagnosticview;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class DiagnosticList extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice;
    private BluetoothSocket socket;

    private TestDataConnection connection;
    private BluetoothConnection btThread;
    private MessageProcessorSingleton messageProcessor;

    private ArrayList<DiagnosticParameter> parameterList;
    private ArrayList<String> optionsArray = new ArrayList<>();

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private String macAddress = null;

    private String TAG = "DiagnosticList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_list);

        adapter = new ArrayAdapter<>(DiagnosticList.this,
                android.R.layout.simple_list_item_activated_1, optionsArray);

        listView = (ListView) findViewById(R.id.list1);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //When an item is selected it will display the chosen items at the bottom of the screen
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String selectedItem = (String)(parent.getItemAtPosition(position));
                //Log.v(TAG, selectedItem);

                SparseBooleanArray checked = listView.getCheckedItemPositions();

                ArrayList<String> selectedArray = new ArrayList<>();
                for(int i = 0; i < listView.getAdapter().getCount(); i++){
                    if(checked.get(i)){
                        selectedArray.add(parameterList.get(i).getLabel());
                    }
                }

                TextView textView = (TextView) findViewById(R.id.display_selected);
                textView.setText(selectedArray.toString());
            }
        });

        //Connect to the sensor and load the metrics
        Load load = new Load();
        load.execute();
    }

    //Parse CSV file and get list of parameters
    private ArrayList<DiagnosticParameter> buildConfigurationParameterList() {
        ArrayList<DiagnosticParameter> sortedArray = new ArrayList<>();

        ArrayList<String[]> configurationList = CSVReader.readIgnoreHeader(new InputStreamReader(getResources().openRawResource(R.raw.diagnostic_parameter_configuration)));

        //Creating list of parameters
        DiagnosticParameter[] parameters = new DiagnosticParameter[configurationList.size()];
        try {
            for (int i = 0; i < configurationList.size(); i++) {
                parameters[i] = DiagnosticParameter.createObject(configurationList.get(i));
            }
        } catch (DiagnosticParameter.CSVParseError csvParseError) {
            csvParseError.printStackTrace();
            return null;
        }

        Collections.sort(Arrays.asList(parameters), new DiagnosticParameterComparator());

        for(int i = 0; i < parameters.length; i++){
            sortedArray.add(parameters[i]);
        }
        return sortedArray;
    }

    //Gets list of chosen items and moves to the next activity
    public void sendMessage(View view){
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<DiagnosticParameter> selectedParameterList = new ArrayList<>();
        for(int i = 0; i < listView.getAdapter().getCount(); i++){
            if(checked.get(i)){
                selectedParameterList.add(parameterList.get(i));
            }
        }

        //Get the Fragment activity and pass the list of parameters
        Intent intent = new Intent(DiagnosticList.this.getApplicationContext(), ViewPagerFragmentActivity.class);
        intent.putParcelableArrayListExtra("fullParameterList", parameterList);
        intent.putParcelableArrayListExtra("selectedParameterList", selectedParameterList);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "OnStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "OnDestroy");
        super.onDestroy();
    }

    class Load extends AsyncTask<String, String, String> {

        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(DiagnosticList.this);
            progDailog.setMessage("Connecting to device and loading statistics");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... aurl) {

            parameterList = buildConfigurationParameterList();

            if (parameterList == null) {
                //Display error about bad config file
            }

            //Get mac address from previous activity
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    macAddress = extras.getString("macAddress");
                }
            }

            //If there is no mac address use test data, else use it for the bluetooth connection
            if (macAddress == null || macAddress.equals("00:00:00:00:00:00")) {
                startTestDataConnection();
            } else {
                try {
                    startBluetoothConnection(macAddress);
                } catch (IOException e) {
                    //Send user to previous screen with error
                    e.printStackTrace();
                }
            }

            //Wait until the list of metrics has been shorted to only reflect the correct data type
            while(!messageProcessor.hasListShortened) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            parameterList = messageProcessor.getParameterList();

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);

            //Populate list of available parameters supported by the sensor
            for(DiagnosticParameter parameter : parameterList){
                optionsArray.add(parameter.getLabel());
            }

            adapter.notifyDataSetChanged();
            progDailog.dismiss();
        }
    }

    public void startBluetoothConnection(String connectionAddress) throws IOException {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.v("Main", "Bluetooth no supported on this device");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        mmDevice = mBluetoothAdapter.getRemoteDevice(connectionAddress);
        socket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        socket.connect();

        btThread = new BluetoothConnection(socket);
        btThread.start();

        messageProcessor = MessageProcessorSingleton.getInstance();
        messageProcessor.setDataConnection(btThread);
        messageProcessor.setParameterList(parameterList);
        messageProcessor.start();
    }

    public void startTestDataConnection() {
        connection = new TestDataConnection(this);
        connection.setMessageDelay(50);

        messageProcessor = MessageProcessorSingleton.getInstance();
        messageProcessor.setDataConnection(connection);
        messageProcessor.setParameterList(parameterList);

        if(!connection.isAlive()){
            connection.start();
        }

        if(!messageProcessor.isAlive()){
            messageProcessor.start();
        }
    }
}