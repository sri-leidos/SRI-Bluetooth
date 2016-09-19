package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.blutooth.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by cassadyja on 4/9/2015.
 */
public class SriBlutoothConnector extends Activity {

    private final static int REQUEST_ENABLE_BT = 10;
    private DevicesAdapter adapter = null;
    private DevicesAdapter discoverAdapter = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BroadcastReceiver mReceiver = null;

    public SriBlutoothConnector() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sri_blutooth_connector_layout);
        ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
        adapter = new DevicesAdapter(this, deviceList);
        ListView listView = (ListView) findViewById(R.id.btV_list);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
            }
        });


        ListView discoverListView = (ListView) findViewById(R.id.btV_discoverlist);
        ArrayList<BluetoothDevice> discoverList = new ArrayList<BluetoothDevice>();
        discoverAdapter = new DevicesAdapter(this, discoverList);
        discoverListView.setAdapter(discoverAdapter);
        discoverListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        discoverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
            }
        });


        connectBT();
        getPairedDevices();
        discoverDevices();

    }

    public void connectBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
        }
    }


    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                adapter.add(device);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void discoverDevices() {
        mBluetoothAdapter.startDiscovery();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    discoverAdapter.add(device);
                }
            }
        };
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "Activity result: " + resultCode, Toast.LENGTH_LONG).show();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
