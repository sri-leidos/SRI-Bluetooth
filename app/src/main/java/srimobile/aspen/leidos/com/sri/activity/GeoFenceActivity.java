package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.*;

import srimobile.aspen.leidos.com.sri.web.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by walswortht on 3/31/2015.
 */
public class GeoFenceActivity extends Activity implements View.OnClickListener,
        GpsStatus.Listener, GpsStatus.NmeaListener, LocationListener {

    Boolean justEnteredFence = false;
    Boolean justExitedFence = false;
    Boolean truckDataSent = false;
    CoordinateChecker coordinateChecker;
    Boolean isInsideFence;
    int i = 0;
    private GPSTracker gps = null;
    private Timestamp tsExitedFence = getNow();
    private boolean mBound;

    private ServiceConnection mConnection;

    public GeoFenceActivity() {
        super();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fence_layout);

//        Button bntRunTests = (Button) findViewById(R.id.btnRunTests);
//        bntRunTests.setOnClickListener(this);

        coordinateChecker = new CoordinateChecker();

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);

        // disable screen lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set screen brightness
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("SRI", "Doing on Start");
        if (mConnection == null) {
            mConnection = new GPSServiceConnection();
        }
        Intent intent = new Intent(this, GPSTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mConnection = null;
            mBound = false;
        }
    }
    @Override
    public void onClick(View v){
//        try {
//            onClickRunTest1(v);
//            Thread.sleep(3000);
//            onClickRunTest2(v);
//            Thread.sleep(3000);
//            onClickRunTest3(v);
//            Thread.sleep(3000);
//            onClickRunTest4(v);
//            Thread.sleep(3000);
//        }
//            onClickRunTest5(v);
//            Thread.sleep(3000);
//            onClickRunTest6(v);
//        } catch (InterruptedException e) {
//            e.printStackTrace();

        coordinateChecker.geoFenceId = null;
        isInsideFence = false;
        justEnteredFence = false;
        justExitedFence = false;
        ((EditText)findViewById(R.id.txtTestRun)).setText("", TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.txtExpectedResult)).setText("", TextView.BufferType.EDITABLE);
        updateTextFields("", "", "", "", "");
    }

    // TEST 1  Coming to approach
    public void onClickRunTest1(View v) {
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 1", TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.txtExpectedResult)).setText(
             "Coming to approach", TextView.BufferType.EDITABLE);
        determineGeoFence(38.554442,-89.925309);
    }

    // TEST 2  Inside approach
    public void onClickRunTest2(View v) {
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 2", TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.txtExpectedResult)).setText(
            "Inside approach", TextView.BufferType.EDITABLE);
        determineGeoFence(38.554879,-89.924125);
    }

    // TEST 3  Leave approach, coming to WIM
    public void onClickRunTest3(View v) {
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 3", TextView.BufferType.EDITABLE);
        ((EditText) findViewById(R.id.txtExpectedResult)).setText(
            "Leave approach, coming to WIM", TextView.BufferType.EDITABLE);
        determineGeoFence(38.555863,-89.924294);
    }

    // TEST 4 Inside WIM
    public void onClickRunTest4(View v) {
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 4", TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.txtExpectedResult)).setText(
            "Inside WIM", TextView.BufferType.EDITABLE);
        determineGeoFence(38.556315,-89.925403);
    }

    // TEST 5 Leave WIM, coming to Exit
    public void onClickRunTest5(View v) {
        // Exit Ramp WIM GeoFence
        // Approaching Exit GeoFence
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 5", TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.txtExpectedResult)).setText(
            "Leave WIM, coming to Exit", TextView.BufferType.EDITABLE);
        determineGeoFence(38.556322,-89.925894);
    }

    // TEST 6 INSIDE EXIT
    public void onClickRunTest6(View v) {
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 6", TextView.BufferType.EDITABLE);
        ((EditText) findViewById(R.id.txtExpectedResult)).setText(
            "Inside Exit", TextView.BufferType.EDITABLE);
        determineGeoFence(8.556143,-89.926527);
    }

    // Test 7 Leave Exit
    public void onClickRunTest7(View v) {
        // Leave the Exit GeoFence
        ((EditText)findViewById(R.id.txtTestRun)).setText(
            "Test Case 7", TextView.BufferType.EDITABLE);
        ((EditText) findViewById(R.id.txtExpectedResult)).setText(
            "Leave Exit", TextView.BufferType.EDITABLE);
        determineGeoFence(38.555382,-89.926341);
    }

    // TEST 8
    public void onClickRunTest8(View v) { }

    public void determineGeoFence(double _lat, double _lon) {

        isInsideFence = coordinateChecker.isGpsContained(_lat, _lon);
        if ((coordinateChecker.geoFenceId).contains("APPROACH")
                || (coordinateChecker.geoFenceId).contains("WIM")) {

            // just entered the fence
            if (isInsideFence && !justEnteredFence) {

                justEnteredFence = true;
                justExitedFence = false;
//                WebServicePoster.postToWebService(getApplicationContext(), _lat, _lon, 1);
                truckDataSent = true;

                // just exited the fence
            } else if (!isInsideFence && justEnteredFence) {

                // only really exit the fence if you've been out of the fence for 5 seconds
                if (getNow().getTime() >= (tsExitedFence.getTime() + 5 * 1000)) {
                    justExitedFence = false;
                    truckDataSent = false;
                } else {
                    tsExitedFence = getNow();
                }

                // everything zen
            } else {
                justEnteredFence = false;
                truckDataSent = false;
            }
        } else if ("EXIT".equals(coordinateChecker.geoFenceId)) {
            truckDataSent = false;
        } else {
            isInsideFence = false;
            justEnteredFence = false;
            truckDataSent = false;
        }


        // this is just used for debugging
        updateTextFields(
            coordinateChecker.geoFenceId,
            isInsideFence.toString(),
            Boolean.toString(truckDataSent),
            Double.toString(_lat),
            Double.toString(_lon));
    }

    private Timestamp getNow() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        Timestamp currentTimestamp = new Timestamp(now.getTime());
        return currentTimestamp;
    }

    private void updateTextFields(String geoFence, String fenceStatus, String dataSent, String latStatus, String lonStatus) {
        ((EditText)findViewById(R.id.txtPostToWebService)).setText(fenceStatus, TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.geoFence_truckDataSent)).setText(dataSent, TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.geoFence_latitude_geo_EdtFld)).setText(latStatus, TextView.BufferType.EDITABLE);
        ((EditText)findViewById(R.id.geoFence_longitude_geo_EdtFld)).setText(lonStatus, TextView.BufferType.EDITABLE);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        determineGeoFence(gps.getLocation().getLatitude(), gps.getLocation().getLongitude());
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        determineGeoFence(gps.getLocation().getLatitude(), gps.getLocation().getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        determineGeoFence(location.getLatitude(), location.getLongitude());
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

    private class GPSServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            gps = ((GPSTracker.GPSTrackerBinder) service).getTheService();
            gps.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, GeoFenceActivity.this);
            gps.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, GeoFenceActivity.this);
            Toast.makeText(GeoFenceActivity.this, "GPS Service Bound", Toast.LENGTH_LONG);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    }
}
