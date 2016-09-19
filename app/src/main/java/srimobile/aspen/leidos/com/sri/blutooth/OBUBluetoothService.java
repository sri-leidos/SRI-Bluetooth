package srimobile.aspen.leidos.com.sri.blutooth;

import android.app.Service;

import srimobile.aspen.leidos.com.sri.data.*;
import srimobile.aspen.leidos.com.sri.gps.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by cassadyja on 4/9/2015.
 */
public class OBUBluetoothService extends Service implements BlueToothServiceInterface, LocationListener {

    private static final UUID MY_UUID = UUID.fromString("66841278-c3d1-11df-ab31-001de000a901");
    private static final String NAME = "AndroidLocomateMessaging";
    private static final String TAG = "SRI-BTS";
    private BluetoothAdapter mAdapter;

    private LocalBroadcastManager manager;

    private int mState;
    private AcceptThread acceptThread;
    private OBUSocketListener socketListener;
    private BluetoothBinder myBinder;
    private BluetoothDevice device;

    private boolean requestPending = false;
    private boolean inWIM = false;
    private int currentMessageSetId;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECTION_LOST = 5;

    private GPSTracker gps = null;
    private boolean mBound;
    private boolean joined = false;
    private String lastRequestId = null;
    private CoordinateChecker checker = null;

    public static final int NO_MESSAGE_SENT = 0;
    public static final int INITIAL_DATA_SENT = 1;
    public static final int ID_RECEIVED = 2;
    public static final int WIM_MESSAGE_SENT = 3;
    public static final int WIM_EXIT_MESSAGE_SENT = 4;
    public static final int BYPASS_NOTIFICATION_RECEIVED = 5;

    private int currentMessageState = 0;

    private BTSocketHandler socketListenerHandler = new BTSocketHandler(this);
    private ServiceConnection mConnection;

    private class GPSServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            gps = ((GPSTracker.GPSTrackerBinder)service).getTheService();
            gps.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, OBUBluetoothService.this);
            gps.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, OBUBluetoothService.this);
            mBound = true;
            manager.sendBroadcast(createBroadcastIntent("GPS Service Bound", OBUMessages.CONNECTION_MESSAGE_RECEIVED));

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    private static class BTSocketHandler extends Handler{

        private final WeakReference<OBUBluetoothService> mservice;

        public BTSocketHandler(OBUBluetoothService theService){
            super();
            mservice = new WeakReference<OBUBluetoothService>(theService);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    String readMessage = new String(readBuf);
                    Log.d(TAG,readMessage);
                    if( OBUMessages.JOINED_MESSAGE.equalsIgnoreCase(readMessage)){

                        if(!mservice.get().isJoined()) {
//                            mservice.get().joined = true;
                            mservice.get().manager.sendBroadcast(mservice.get().createBroadcastIntent(readMessage, OBUMessages.CONNECTION_MESSAGE_RECEIVED));
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mservice.get().performJoinedAction();
                        }else{
                            Log.d(TAG, "Received Join, but already Joined");
                        }
                    }else if(OBUMessages.OUT_OF_RANGE.equalsIgnoreCase(readMessage)){
                        //Left RSU
                        mservice.get().manager.sendBroadcast(mservice.get().createBroadcastIntent(readMessage, OBUMessages.CONNECTION_MESSAGE_RECEIVED));
                        mservice.get().setJoined(false);
                        mservice.get().resetState();
                    }else{
                        // have a server response
                        mservice.get().processServerResponse(readBuf);
                    }
                    break;
                case MESSAGE_CONNECTION_LOST:
                    mservice.get().startAcceptThread();
                    mservice.get().setJoined(false);
                    mservice.get().resetState();

                    break;
            }

        }
    };


    public class BluetoothBinder extends Binder{
        private BlueToothServiceInterface service;

        public BlueToothServiceInterface getService() {
            return service;
        }

        public void setService(BlueToothServiceInterface service) {
            this.service = service;
        }
    }


    private Intent createBroadcastIntent(String msg, String category){
        Intent intent = new Intent("SRI-BT-Event");
        intent.putExtra(OBUMessages.MESSAGE_STRING_EXTRA,msg);
        intent.addCategory(category);
        return intent;
    }


    private void resetState(){
        requestPending = false;
        currentMessageState = NO_MESSAGE_SENT;
        currentMessageSetId = -1;
    }

    protected void performJoinedAction(){
//        if(!requestPending){
            sendMessage(createJoinedMessage());
            currentMessageState = INITIAL_DATA_SENT;
//        }else{
//            checkStatus();
//        }
    }

    protected void sendMessage(DriverVehicleInformationData dataToSend){
        Log.d(TAG, "Strarting Send message");
        Log.d(TAG, "Encoding message with SiteId: "+dataToSend.getSiteId());

        manager.sendBroadcast(createBroadcastIntent("Encoding Message", OBUMessages.CONNECTION_MESSAGE_RECEIVED));
        byte[] encodedMessage = DSRCCoder.encodeVehicleData(dataToSend);
        String s = bytesToHex(encodedMessage);
        Log.d(TAG,"Encoded message is: "+s);
        if(encodedMessage != null) {
            Log.d(TAG,"Passing message to socketListener");
//            manager.sendBroadcast(createBroadcastIntent("Writing Message bytes to Socket", OBUMessages.CONNECTION_MESSAGE_RECEIVED));
            OBUSocketListener r = null;
            synchronized (this){
                r = socketListener;
            }
            r.writeData(encodedMessage);
//            manager.sendBroadcast(createBroadcastIntent("Message written to Socket", OBUMessages.CONNECTION_MESSAGE_RECEIVED));
            requestPending = true;
        }else{
            Log.e(TAG, "Encountered an Error encoding message to server");
        }

    }


    private DriverVehicleInformationData createGenericMessage(String siteId){
        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckV_driversLicense = preferences.getString("cdl", "Commercial Driver's License");
        String driversLicense =  preferences.getString("dl", "Driver's License");
        String truckV_vin = preferences.getString("vin", "Vehicle Identification Number");
        String truckV_usdot = preferences.getString("usdot", "USDOT Number");
        String truckV_lp = preferences.getString("lp", "License Plate");

        Location loc = gps.getLocation();
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();

        DriverVehicleInformationData data = new DriverVehicleInformationData();
        if(true) {
            data.setCdlNumber(truckV_driversLicense);
        }else {
            data.setDriversLicenseNumber(driversLicense);
        }

        data.setPlateNumber(truckV_lp);
        data.setUsdotNumber(truckV_usdot);
        data.setVin(truckV_vin);

        Latitude latitude = new Latitude();
        latitude.setValue((int)(lat * 10000000));
        Longitude longitude = new Longitude();
        longitude.setValue((int)(lon * 10000000));
        data.setLat(latitude);
        data.setLon(longitude);
        data.setFullDate(getDateTimeForTimeStamp());
        data.setSiteId(siteId);
        return data;
    }


    protected DriverVehicleInformationData createJoinedMessage(){
        DriverVehicleInformationData data = createGenericMessage(OBUMessages.SITE_ID+OBUMessages.APPROACH_ADDITION);
        Log.d("BTS","Set site ID for approach: "+data.getSiteId());
        return data;
    }

    private DriverVehicleInformationData createWimEnterMessage(){
        DriverVehicleInformationData data = createGenericMessage(OBUMessages.SITE_ID+OBUMessages.WIM_ENTER_ADDITION);
        data.setId(currentMessageSetId+"");
        return data;
    }

    private DriverVehicleInformationData createWimExitMessage(){
        DriverVehicleInformationData data = createGenericMessage(OBUMessages.SITE_ID+OBUMessages.WIM_EXIT_ADDITION);
        data.setId(currentMessageSetId+"");
        return data;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private DDateTime getDateTimeForTimeStamp() {
        Calendar cal = Calendar.getInstance();
        DDateTime dateTime = new DDateTime();
        DHour hour = new DHour(cal.get(Calendar.HOUR_OF_DAY));
        dateTime.setHour(hour);
        DMinute min = new DMinute(cal.get(Calendar.MINUTE));
        dateTime.setMinute(min);
        DSecond sec = new DSecond(cal.get(Calendar.SECOND));
        dateTime.setSecond(sec);
        DMonth month = new DMonth(cal.get(Calendar.MONTH)+1);
        dateTime.setMonth(month);
        DDay day = new DDay(cal.get(Calendar.DAY_OF_MONTH));
        dateTime.setDay(day);
        DYear year = new DYear(cal.get(Calendar.YEAR));
        dateTime.setYear(year);
        return dateTime;
    }


//    protected byte[] encodeData(DriverVehicleInformationData dataToSend){
//        try {
//            Log.d("BTS","Starting Encode Data, siteId: "+dataToSend.getSiteId());
//            IEncoder<DriverVehicleInformationData> encoder = CoderFactory.getInstance().newEncoder("BER");
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            encoder.encode(dataToSend, baos);
//            return baos.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }
//
//    private DriverVehicleInformationData decodeData(byte[] bytes){
//
//        try {
//            IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
//            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//            DriverVehicleInformationData data = decoder.decode(bais, DriverVehicleInformationData.class);
//            return data;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }


    /**
     *  Called when we have already made a request to SRI
     *  but have not received a Red or Green
     *  and just joined with a new RSU
     */
    protected void checkStatus(){
        Log.d(TAG, "Sending check status message");



    }

    protected void processServerResponse(byte[] readMessage){
        Log.d(TAG,"Received message from OBU: "+readMessage.length+" bytes long");
        switch(currentMessageState){
            case INITIAL_DATA_SENT:{
                //message received;
                Log.d(TAG,"Response from initial data received");
                DriverVehicleInformationData data = DSRCCoder.decodeVehicleData(readMessage);
                currentMessageSetId = Integer.parseInt(data.getId());
                Log.d(TAG, "Received data Id from backend: " + currentMessageSetId);
                setJoined(true);

                manager.sendBroadcast(createBroadcastIntent(OBUMessages.JOINED_MESSAGE, OBUMessages.PASS_FAIL_MESSAGE_RECEIVED));

                manager.sendBroadcast(createBroadcastIntent("Id received: "+currentMessageSetId+"  "+data.getId(), OBUMessages.CONNECTION_MESSAGE_RECEIVED));

                requestPending = false;
                break;
            }
            case WIM_MESSAGE_SENT:{
                Log.d(TAG,"WIM Enter response Received");
                //This response doesn't matter, the server should just give us a 200 OK.
                requestPending = false;
                break;
            }
            case WIM_EXIT_MESSAGE_SENT:{
                //message received should contain our red or green
                WeightResultData data = DSRCCoder.decodeWeightResultData(readMessage);
                Log.d(TAG,"Response from WIM Message received");
                if(data.getWeightResult().equalsIgnoreCase(OBUMessages.WEIGHT_PASSED)){
                    manager.sendBroadcast(createBroadcastIntent(OBUMessages.WEIGHT_PASSED, OBUMessages.PASS_FAIL_MESSAGE_RECEIVED));
                }else if(data.getWeightResult().equalsIgnoreCase(OBUMessages.WEIGHT_FAILED)){
                    manager.sendBroadcast(createBroadcastIntent(OBUMessages.WEIGHT_FAILED, OBUMessages.PASS_FAIL_MESSAGE_RECEIVED));
                }
                resetState();
                break;
            }
        }
    }


    private WeightResultData decodeWeighResultData(byte[] bytes){
        WeightResultData data = null;
        try {
            IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            data = decoder.decode(bais, WeightResultData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void setBluetoothDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void connectBluetooth() {

    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "Doing Bluetooth connected method");
        this.device = device;
        this.socketListener = new OBUSocketListener(socket, socketListenerHandler);
        manager.sendBroadcast(createBroadcastIntent("Connected to OBU", OBUMessages.CONNECTION_MESSAGE_RECEIVED));
        Log.d(TAG, "Starting socket Listener thread");
        new Thread(socketListener).start();
        setState(STATE_CONNECTED);

    }

    public void onCreate(){
        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        Log.d(TAG,"~~~~~~~ Creating BT Service");
        checker = new CoordinateChecker();

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG,"~~~~~~~ Starting BT Accept Thread");
        startAcceptThread();

        if(mConnection == null){
            mConnection = new GPSServiceConnection();
        }
        Log.d(TAG,"~~~~~~~ BT Connecting to GPS");
        Intent intent = new Intent(this, GPSTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"~~~~~~~ BT Service Created");

    }




    public void onDestroy(){
        acceptThread.cancel();
        if(socketListener != null) {
            socketListener.cancel();
        }
        socketListener = null;
        acceptThread = null;
        setState(STATE_NONE);
        if(mBound){
            Log.d(TAG,"Unbinding GPS");
            gps.getLocationManager().removeUpdates(this);
            Log.d(TAG,"Removed GPS Updates");
            unbindService(mConnection);
            Log.d(TAG,"GPS unbound");
        }
    }

    private synchronized boolean isJoined(){
        return joined;
    }

    private synchronized void setJoined(boolean b){
        joined = b;
    }

    protected void startAcceptThread() {
        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
    }




    @Override
    public IBinder onBind(Intent intent) {
        if(myBinder == null){
            myBinder = new BluetoothBinder();
            myBinder.setService(this);
        }



        return myBinder;
    }




    private synchronized void setState(int state) {
        this.mState = state;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private boolean canceled = false;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            		/* For android versions less than gingerbread(2.3.3) */
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                } else {
            		/* For android versions gingerbread(2.3.3) and above */
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }



        public void run(){
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED && !canceled) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                    Log.d(TAG, "Accepted Socket connect socket: "+socket);
                } catch (IOException e) {

                    if(!e.getLocalizedMessage().equalsIgnoreCase("Try again")) {
                        Log.w(TAG, "accept() failed", e);
                        break;
                    }else{
                        Log.i(TAG, "accept() timeout");
                    }
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (OBUBluetoothService.this) {
                        connected(socket, socket.getRemoteDevice());
                        //This code was not working, I think I was setting the state wrong
                        //since it would never call the connected method.
//                        switch (mState) {
//                            case STATE_LISTEN:
//                            case STATE_CONNECTING:
//                                // Situation normal. Start the connected thread.
////                                connected(socket, socket.getRemoteDevice());
//                                break;
//                            case STATE_NONE:
//                            case STATE_CONNECTED:
//                                // Either not ready or already connected. Terminate new socket.
//                                try {
//                                    socket.close();
//                                } catch (IOException e) {
//                                    Log.e(TAG, "Could not close unwanted socket", e);
//                                }
//                                break;
//                        }
                    }
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            canceled = true;
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location Changed");
//        manager.sendBroadcast(createBroadcastIntent("Location Changed", OBUMessages.CONNECTION_MESSAGE_RECEIVED));
        String gateName = checker.gate_name_coordinate(location.getLatitude(), location.getLongitude());
        if(isJoined()){
            //If we are joined and we have entered the WIM GeoFence then send the WIM Enter message.
            //If we are joined and we are exiting the WIM GeoFence (GPS says we are not in the
            // fence, but the inWIM flag is true then we have exited) then send the WIM Exit Message.
            Log.d(TAG, "Have gate name of: "+gateName);
            if(gateName != null && gateName.indexOf("WIM")>-1) {
                //Send message now, we are in the WIM fence and joined to RSU.
                if (!inWIM) {
                    if(currentMessageSetId > -1) {
                        inWIM = true;
                        sendMessage(createWimEnterMessage());
                        currentMessageState = WIM_MESSAGE_SENT;
                    }else{
                        Log.d(TAG,"No Message Set Id");
                    }
                }
            }else{
                if(inWIM){
                    inWIM = false;
                    sendMessage(createWimExitMessage());
                    currentMessageState = WIM_EXIT_MESSAGE_SENT;

                }

            }

        }else{
            //Not in RSU range.
            Log.d(TAG,"Not in RSU range");
        }

        if(gateName != null && gateName.indexOf("EXIT")>-1 ){
            manager.sendBroadcast(createBroadcastIntent(OBUMessages.STATION_EXIT, OBUMessages.PASS_FAIL_MESSAGE_RECEIVED));
            currentMessageState = NO_MESSAGE_SENT;
            setJoined(false);
            requestPending = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
