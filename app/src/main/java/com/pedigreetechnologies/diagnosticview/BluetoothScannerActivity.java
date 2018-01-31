package com.pedigreetechnologies.diagnosticview;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothScannerActivity extends AppCompatActivity {

    private ListView deviceListView;
    private ArrayList<SBluetoothDevice> deviceList = new ArrayList();
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter deviceAdapter;
    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scanner);

        //Adding test bluetooth device to access the test data
        deviceList.add(new SBluetoothDevice("Test Device", "00:00:00:00:00:00"));

        deviceListView = (ListView) findViewById(R.id.listView);
        deviceAdapter = new DeviceAdapter(this, deviceList);
        deviceListView.setAdapter(deviceAdapter);

        //Sets list items onclick to open new page and connect to device
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SBluetoothDevice device = deviceAdapter.getItem(position);
                Intent intent = new Intent(BluetoothScannerActivity.this.getApplicationContext(), DiagnosticList.class);
                intent.putExtra("macAddress", device.deviceAddress);
                startActivity(intent);
            }
        });
    }

    public void scan(View v) {
        deviceList.clear();
        deviceList.add(new SBluetoothDevice("Test Device", "00:00:00:00:00:00"));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check user permission for bluetooth use
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously*
            } else {
                // No explanation needed, we can request the permission.
                int response = 0;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, response);
            }
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        isReceiverRegistered = true;
    }

    @Override
    protected void onStop() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        if (isReceiverRegistered) {
            unregisterReceiver(mReceiver);
            isReceiverRegistered = false;
        }

        Log.v("scanneract", "OnStop");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v("scanneract", "OnDestroy");
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                SBluetoothDevice discoveredDevice = new SBluetoothDevice(deviceName, deviceHardwareAddress);

                //Check if its is an LMU device and it isn't already in the list
                if (deviceName != null && deviceName.contains("LMU") && !deviceList.contains(discoveredDevice)) {
                    deviceList.add(discoveredDevice);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private class SBluetoothDevice {
        private String deviceName;
        private String deviceAddress;

        public SBluetoothDevice(String deviceName, String deviceAddress) {
            this.deviceName = deviceName;
            this.deviceAddress = deviceAddress;
        }

        public String getName() {
            return deviceName;
        }

        public String getAddress() {
            return deviceAddress;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SBluetoothDevice)) {
                return false;
            }

            SBluetoothDevice device = (SBluetoothDevice) obj;

            return this.deviceName.equals(device.deviceName) && this.deviceAddress.equals(device.deviceAddress);
        }
    }

    private class DeviceAdapter extends ArrayAdapter<SBluetoothDevice> {

        public DeviceAdapter(Context context, ArrayList<SBluetoothDevice> devices) {
            super(context, 0, devices);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            SBluetoothDevice device = getItem(position);

            // Lookup view for data population
            TextView deviceName = (TextView) convertView.findViewById(R.id.device_name);
            deviceName.setText(device.getName());

            TextView deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            deviceAddress.setText(device.getAddress());

            return convertView;
        }
    }
}

