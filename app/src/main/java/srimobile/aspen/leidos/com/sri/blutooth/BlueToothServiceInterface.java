package srimobile.aspen.leidos.com.sri.blutooth;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

/**
 * Created by cassadyja on 4/10/2015.
 */
public interface BlueToothServiceInterface {

    public void setBluetoothDevice(BluetoothDevice device);

    public void connectBluetooth();


}
