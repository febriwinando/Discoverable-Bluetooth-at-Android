package com.example.appsbluetoothdiscoverable;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    static BluetoothAdapter bluetoothAdapter;
    private TextView switchStatusBluetooth;
    private Switch switchBluetooth;
    private Button btnSeacrhBluetooth;
    private ListView lvNewDevices;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter devicesListAdapter;

    private  final BroadcastReceiver broadcastReceiver1= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive : State Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "broadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "broadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "broadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverbility Enable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiver2: Discoverbility Enable. able to Receive Connection");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcastReceiver2: Discoverbility Disable. Not Able to Receive Connection");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "broadcastReceiver2: Connecting");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcastReceiver2: Connected");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);

                Log.d(TAG, "onReceive" + device.getName()+ ": "+device.getAddress());
                devicesListAdapter = new DeviceListAdapter(context, R.layout.view_adapter, mBTDevices);
                lvNewDevices.setAdapter(devicesListAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTDevices = new ArrayList<>();


        switchStatusBluetooth = (TextView) findViewById(R.id.switchStatusBluetooth);
        switchBluetooth = (Switch) findViewById(R.id.switchBluetooth);
        btnSeacrhBluetooth = (Button)findViewById(R.id.btnSearchBluetooth);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);


        //set switch to Off
        switchBluetooth.setChecked(false);

        //attach a listener to check for changes in state
        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    enableDisableBT();
                    switchStatusBluetooth.setText("Status Bluetooth: ON");
                } else {
                    disableBt();
                    switchStatusBluetooth.setText("Status Bluetooth: OFF");
                }

            }
        });
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //check the current state before we display the screen
        if(switchBluetooth.isChecked()){
            switchStatusBluetooth.setText("Status: ON");
        }
        else {
            switchStatusBluetooth.setText("Status: OFF");
        }

        btnSeacrhBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBluetooth();
            }
        });
    }

    private void searchBluetooth() {
        Log.d(TAG, "btnDiscover: Looking for unpaired Devices.");

        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Cancel Discovering.");
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, discoverDeviceIntent);
        }

        if (!bluetoothAdapter.isDiscovering()){
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, discoverDeviceIntent);
        }
    }

    public void enableDisableBT(){
        if (bluetoothAdapter== null ){
            Log.d(TAG, "enableDisableBT: Does not have Bluetooth Compabilities");
        }

        if (!bluetoothAdapter.isEnabled()){
            Log.d(TAG, "enable Bluetooth: enabling Bluetooth");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver1, BTIntent);
        }

        btnEnableDisable_Discoverable();
    }

    public  void disableBt(){
        Log.d(TAG, "Disable BT: disabling Bluetooth");
        bluetoothAdapter.disable();

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver1, BTIntent);
    }

    //make bluetooth discoverable
    public void btnEnableDisable_Discoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making Device discoverable  for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiver2, intentFilter);
    }

    //tambahan 2
    private void checkBTPermissions(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }else {
            Log.d(TAG, "checkBTPermissions: No Need to Check permissions. SDK version < LOLLIPOP");
        }
    }
}
