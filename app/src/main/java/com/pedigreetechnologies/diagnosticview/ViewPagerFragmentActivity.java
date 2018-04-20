package com.pedigreetechnologies.diagnosticview;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Vector;


public class ViewPagerFragmentActivity extends AppCompatActivity {

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

    private DrawerLayout mDrawerLayout;

    private PagerAdapter mPagerAdapter;
    ViewPager pager;
    ToggleButton toggleAB;
    int pagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        Intent intent = getIntent();

        // get rid of app name being displayed in views
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.initialisePaging(extras);
            }
        }

        // Adding a listener for when the page changes
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            // Sets the correct image when the page changes, used incase the page gets changed by gestures
            public void onPageSelected(int pagePosition) {
                // Check if this is the page you want.
                toggleAB = findViewById(R.id.toggle_ab);
                if (pagePosition == 1)
                    toggleAB.setChecked(true);
                else
                    toggleAB.setChecked(false);
            }
        });


        adapter = new ArrayAdapter<>(ViewPagerFragmentActivity.this,
                android.R.layout.simple_list_item_activated_1, optionsArray);


        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight

                        //NavigationView navigationView1 = findViewById(R.id.nav_view);
                        Menu menu = navigationView.getMenu();

                        menuItem.setChecked(true);


                        if (navigationView.getMenu().getItem(6).isChecked()) {
                            navigationView.getMenu().getItem(6).setChecked(false);
                        } else {
                            navigationView.getMenu().getItem(6).setChecked(true);
                        }

                        //listView.setItemChecked(2, true);
                        //listView.setSelection(2);


                        //Get the Fragment activity and pass the list of parameters
//                        Intent intent = new Intent(ViewPagerFragmentActivity.this.getApplicationContext(), ViewPagerFragmentActivity.class);
//                        intent.putParcelableArrayListExtra("fullParameterList", parameterList);
//                        intent.putExtra("selectedParameterList", navigationView.getMenu().getItem(6).getTitle());
//                        System.out.println(navigationView.getMenu().getItem(6).getTitle());


                        //intent.putParcelableArrayListExtra("selectedParameterList", navigationView.getMenu().getItem(6).getTitle());
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        //startActivity(intent);

                        // navigationView.getMenu().getItem(6).getActionView().findViewById(R.layout.switch_item));
//                        menu.findItem(R.id.metrics).getActionView().findViewById(R.id.toggle_ab);
//                        sendMessage(navigationView);
                        //}
                        //mDrawerLayout.openDrawer(Gravity);
                        // close drawer when item is tapped
                        //mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        //Connect to the sensor and load the metrics
        ViewPagerFragmentActivity.Load load = new ViewPagerFragmentActivity.Load();
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

        for (int i = 0; i < parameters.length; i++) {
            sortedArray.add(parameters[i]);
        }
        return sortedArray;
    }

    //Gets list of chosen items and moves to the next activity
    public void sendMessage(View view) {
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<DiagnosticParameter> selectedParameterList = new ArrayList<>();
        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                selectedParameterList.add(parameterList.get(i));
            }
        }

        //Get the Fragment activity and pass the list of parameters
        Intent intent = new Intent(ViewPagerFragmentActivity.this.getApplicationContext(), ViewPagerFragmentActivity.class);
        intent.putParcelableArrayListExtra("fullParameterList", parameterList);
        intent.putParcelableArrayListExtra("selectedParameterList", selectedParameterList);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            progDailog = new ProgressDialog(ViewPagerFragmentActivity.this);
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
            while (!messageProcessor.hasListShortened) {
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

            NavigationView navigationView1 = findViewById(R.id.nav_view);
            Menu menu = navigationView1.getMenu();
            int i = 0;
            //Populate list of available parameters supported by the sensor
            for (DiagnosticParameter parameter : parameterList) {
                optionsArray.add(parameter.getLabel());
                i += 1;

                menu.add(R.id.metrics, i, Menu.NONE, parameter.getLabel()).setActionView(R.layout.switch_item);
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

        if (!connection.isAlive()) {
            connection.start();
        }

        if (!messageProcessor.isAlive()) {
            messageProcessor.start();
        }
    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging(Bundle extras) {

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, TabGaugeFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TabChartFragment.class.getName()));

        for (int i = 0; i < fragments.size(); i++) {
            fragments.get(i).setArguments(extras);
        }

        this.mPagerAdapter = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        pager = (ViewPager) super.findViewById(R.id.viewpager);
        pager.setAdapter(this.mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Switch button that changes between gauge and graph view
        toggleAB = menu.findItem(R.id.mytoggle).getActionView().findViewById(R.id.toggle_ab);
        toggleAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // Switches to graph view
                    // pagePosition is redundant but is used for pageListener
                    pagePosition = 1;
                    pager.setCurrentItem(pagePosition, true);

                } else {
                    // Switches to gauge view
                    // pagePosition is redundant but is used for pageListener
                    pagePosition = 0;
                    pager.setCurrentItem(pagePosition, true);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}