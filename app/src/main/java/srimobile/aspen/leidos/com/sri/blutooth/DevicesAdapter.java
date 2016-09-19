package srimobile.aspen.leidos.com.sri.blutooth;

import srimobile.aspen.leidos.com.sri.R;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cassadyja on 4/9/2015.
 */
public class DevicesAdapter extends ArrayAdapter<BluetoothDevice> {


    public DevicesAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, android.R.layout.simple_list_item_single_choice, devices);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.btdevice_layout, parent, false);
        }
        // Lookup view for data population
        TextView btName = (TextView) convertView.findViewById(R.id.btV_deviceName);

        // Populate the data into the template view using the data object
        btName.setText(device.getName());

        // Return the completed view to render on screen
        return convertView;
    }
}

