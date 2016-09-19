package srimobile.aspen.leidos.com.sri.blutooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by cassadyja on 4/9/2015.
 */
public class OBUSocketListener implements Runnable {
    private static final String TAG = "SRI";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;

    public OBUSocketListener(BluetoothSocket socket, Handler handler) {
        this.mmSocket = socket;
        this.handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("SRI", e.toString());
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        //listen for message with Read
        //once bytes have been read send message to handler
        //return to reading

        int bytes = -1;
        // Keep listening to the InputStream while connected
        Log.d("BTSL","Listening for data");
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                // Read from the InputStream
                Log.d("BTSL","Calling read on stream waiting...");
                bytes = mmInStream.read(buffer);
                byte[] resized = Arrays.copyOf(buffer, bytes);
                Log.d("BTSL","Read bytes from input stream: "+bytes);
                // Send the obtained bytes to the UI BT Service
                handler.obtainMessage(OBUBluetoothService.MESSAGE_READ, bytes, -1, resized)
                        .sendToTarget();
            } catch (Exception e) {
                Log.e(TAG, "disconnected", e);
                try {
                    cancel();
                }catch(Exception e1){
                    e.printStackTrace();
                }
                handler.obtainMessage(OBUBluetoothService.MESSAGE_CONNECTION_LOST, 0, -1, new byte[]{0})
                        .sendToTarget();
                break;
            }
        }

    }


    public void writeData(byte[] data) {
        Log.d("BTSL","Writing Data to output stream size: "+data.length);
        try {
            mmOutStream.write(data);
            mmOutStream.flush();
        } catch (IOException e) {
            Log.e("SRI", e.toString());
        }
    }

    public void cancel() {
        try {
            if(mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.d("SRI", e.toString());
        }
    }


}
