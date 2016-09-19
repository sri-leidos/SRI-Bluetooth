package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.blutooth.*;
import srimobile.aspen.leidos.com.sri.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;



/**
 * Created by walswortht on 5/14/2015.
 */
public class RedGreenSignalActivity extends Activity {

    DrawingView drawingView;
    public static int REQUEST_CODE = 12345;


    private boolean btBound = false;
    private ServiceConnection btConnection;
    private BlueToothServiceInterface btService;

    private BluetoothAdapter mAdapter;
    private static final int REQUEST_ENSURE_DISCOVERABLE = 2;
    private LinearLayout linLayout;

    private boolean forwardedToParkingActivity = false;
    private boolean joined = false;
    private boolean isWeightOver = false;


    private MyBroadcastReceiver receiver = new MyBroadcastReceiver();

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SRI","Received Broadcast at Red Green["+intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA)+"]");
            if(intent.getCategories().contains(OBUMessages.CONNECTION_MESSAGE_RECEIVED)) {
                Toast.makeText(RedGreenSignalActivity.this, intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA), Toast.LENGTH_SHORT).show();
                if(intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA).equalsIgnoreCase(OBUMessages.OUT_OF_RANGE)){
                    //We are out of range of the RSU.  Need to reset screen and settings.
                    joined = false;
                    drawingView.drawLogo();
                }
            }else if(intent.getCategories().contains(OBUMessages.PASS_FAIL_MESSAGE_RECEIVED)){
                Toast.makeText(RedGreenSignalActivity.this, intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA), Toast.LENGTH_SHORT).show();
                if(intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA).equalsIgnoreCase(OBUMessages.JOINED_MESSAGE)){
                    forwardedToParkingActivity = false;
                    joined = true;
                    Log.d("SRI", "Switching to Entered Screen");
                    drawingView.initialWeighStationEntered();
                }else if(intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA).equalsIgnoreCase(OBUMessages.WEIGHT_PASSED)){
                    isWeightOver = false;
                    drawingView.drawSignal();
                }else if(intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA).equalsIgnoreCase(OBUMessages.WEIGHT_FAILED)){
                    isWeightOver = true;
                    drawingView.drawSignal();
                }else if(intent.getStringExtra(OBUMessages.MESSAGE_STRING_EXTRA).equalsIgnoreCase(OBUMessages.STATION_EXIT)){
                    joined = false;
//                    sendToTruckParkingScreen();
                    drawingView.drawLogo();
                }
            }
        }
    }

    private class BluetoothServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            btService = ((OBUBluetoothService.BluetoothBinder) service).getService();
            Toast.makeText(RedGreenSignalActivity.this, "BT Service Bound", Toast.LENGTH_LONG).show();
            if(joined){
                drawingView.initialWeighStationEntered();
            }else{
                drawingView.drawLogo();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btBound = false;
        }
    }



    private void ensureDiscoverable() {
        if (mAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            //handle the return value of this intent to setup the chat session
            startActivityForResult(discoverableIntent, REQUEST_ENSURE_DISCOVERABLE);
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter("SRI-BT-Event");
        filter.addCategory(OBUMessages.PASS_FAIL_MESSAGE_RECEIVED);
        filter.addCategory(OBUMessages.CONNECTION_MESSAGE_RECEIVED);
        manager.registerReceiver(receiver, filter);

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
        	/* For android versions less than jellybean(4.3) */
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            Toast.makeText(this, "Android version < 4.3", Toast.LENGTH_LONG).show();
        } else {
    		/* For android versions jellybean(4.3) and above */
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mAdapter = bluetoothManager.getAdapter();
            Toast.makeText(this, "JellyBean 4.3/ kitkat 4.4", Toast.LENGTH_LONG).show();
        }

        drawingView = new DrawingView(this);
        SurfaceCallback surfaceCallback = new SurfaceCallback();
        drawingView.getHolder().addCallback(surfaceCallback);

        // disable screen lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowManager.LayoutParams layout = getWindow().getAttributes();
        getWindow().setAttributes(layout);

    }


    private Timestamp getNow() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        return new Timestamp(now.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("SRI-RGA", "Doing on Start");

        ensureDiscoverable();
        if(btConnection == null){
            btConnection = new BluetoothServiceConnection();
        }

        Intent btIntent = new Intent(getApplication(), OBUBluetoothService.class);
        bindService(btIntent, btConnection, Context.BIND_AUTO_CREATE);

//        sendToTruckParkingScreen();
        LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        dummyParams.weight = 1f;

        // START ADDING WEIGH STATION VIEW
        linLayout = new LinearLayout(this);
        linLayout.setLayoutParams(dummyParams);
        linLayout.addView(drawingView);

        // CREATE weighStationDynamic FROM weigh_station_dynamic_layout
//        LinearLayout wsdv = (LinearLayout)findViewById(R.id.weighStationDynamic);
//        wsdv.addView(linLayout);
        this.addContentView(linLayout, dummyParams);
    }

    private void sendToTruckParkingScreen(){
        if(!forwardedToParkingActivity) {
            Intent explicitIntent = new Intent(RedGreenSignalActivity.this, TruckSmartParking.class);
            forwardedToParkingActivity = true;
            startActivity(explicitIntent);
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        linLayout.removeView(drawingView);
        // Unbind from the service
        if(btBound){
            Log.d("SRI-RGA","Unbinding BT");
            unbindService(btConnection);
            btConnection = null;
            btBound = false;
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            forwardedToParkingActivity = false;
        }
    }


    class SurfaceCallback implements SurfaceHolder.Callback {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder.getSurface().isValid()) {


            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    class DrawingView extends SurfaceView {
//        private GPSTracker gpsTracker;

        private final SurfaceHolder surfaceHolder;
//        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

//        private boolean isWeighStation_weightRequested;

//        Paint redCirclePaint = new Paint();
//        Paint greenCirclePaint = new Paint();

//        Paint geoFenceTextTest = new Paint();
//        Paint geoFenceText = new Paint();

//        CoordinateChecker coordinateChecker = new CoordinateChecker();

        public DrawingView(Context context) {
            super(context);
            surfaceHolder = getHolder();

            isWeightOver = false;
        }



        private void drawLogo(){
            if(surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                Paint p = new Paint();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);            // SET BACKGROUND
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.sri_logo);

                    Double imgWidth = new Double(b.getWidth());
                    Double imgHeight = new Double(b.getHeight());
                    Double layoutWidth = new Double(this.getWidth());
                    Double layoutHeight = new Double(this.getHeight());
//
//                    Double scale = (imgWidth - layoutWidth) / imgWidth;
//                    scale = imgHeight - (scale * imgHeight);
//
                    int midpoint = (int) (layoutHeight / 2);
                    int drawStartX = (int)(layoutWidth/2) - (int)(imgWidth/2);
                    int drawStartY = midpoint - (int)(imgHeight/2);
//                    b = b.createScaledBitmap(b, this.getWidth(), this.getHeight(), true);
                    canvas.drawBitmap(b, drawStartX, drawStartY, p);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Log.d("SRIRGA", "Canvas was null");
                }
            }else{
                Log.d("SRIRGA", "Surface was not valid");
            }
        }

        private void initialWeighStationEntered() {
            if(surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                Paint p = new Paint();
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK);            // SET BACKGROUND
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_approaching_image);

                    Double imgWidth = new Double(b.getWidth());
                    Double imgHeight = new Double(b.getHeight());
                    Double layoutWidth = new Double(this.getWidth());
                    Double layoutHeight = new Double(this.getHeight());

                    Double scale = (imgWidth - layoutWidth) / imgWidth;
                    scale = imgHeight - (scale * imgHeight);

                    int midpoint = (int) (layoutHeight / 2);

                    b = b.createScaledBitmap(b, this.getWidth(), this.getHeight(), true);
                    canvas.drawBitmap(b, 0, 0, p);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Log.d("SRIRGA", "Canvas was null");
                }
            }else{
                Log.d("SRIRGA", "Surface was not valid");
            }
        }


//        public void weighStation_exitedFacility() {
//            Canvas canvas = surfaceHolder.lockCanvas();
//            String finalVal = "EXITING WEIGH STATION";
//
//            Paint paint = new Paint();
//            paint.setTextSize(80);
//            paint.setTypeface(Typeface.SANS_SERIF);
//
//            Rect bounds = new Rect();
//            paint.getTextBounds(finalVal, 0, finalVal.length(), bounds);
//            int lh = bounds.height();
//            int y = ((getHeight() - (lh * 6)) / 2);
//
//            canvas.drawColor(0, PorterDuff.Mode.CLEAR);         // EMPTY CANVAS
//            canvas.drawColor(Color.parseColor("#CCCCCC"));
//
//            geoFenceText.setTextSize(80);
//            geoFenceText.setColor(Color.WHITE);     // SET TEXT COLOR
//            geoFenceText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//
//            drawText(canvas, geoFenceText, "EXITING", y);
//            drawText(canvas, geoFenceText, "WEIGH STATION FACILITY", y + lh * 2);
//        }


//        public void drawTestButtons(Canvas canvas, Location location) {
//
//            geoFenceTextTest.setTextSize(100);
//            geoFenceTextTest.setColor(Color.CYAN);
//
//            drawText(canvas, geoFenceTextTest, "LAT:" + location.getLatitude(), getHeight() - 400);
//            drawText(canvas, geoFenceTextTest, "LONG:" + location.getLongitude(), getHeight() - 300);
//            drawText(canvas, geoFenceTextTest, "STT:" + coordinateChecker.weighStationName_name_coordinate(location.getLatitude(), location.getLongitude()), getHeight() - 200);
//            drawText(canvas, geoFenceTextTest, "GATE:" + coordinateChecker.gate_name_coordinate(location.getLatitude(), location.getLongitude()), getHeight() - 100);
//            drawText(canvas, geoFenceTextTest, "5SEC:" + time5seconds, getHeight() - 0);
//        }


        public void drawSignal() {
            Canvas canvas = surfaceHolder.lockCanvas();


            Paint p = new Paint();
            canvas.drawColor(Color.BLACK);            // SET BACKGROUND

            Bitmap b = null;

            if (!isWeightOver) {
                b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_passed_image);
            } else {
                b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_failed_image);
            }
            Double imgWidth = new Double(b.getWidth());
            Double imgHeight = new Double(b.getHeight());
            Double layoutWidth = new Double(this.getWidth());
            Double layoutHeight = new Double(this.getHeight());

            Integer scaleW = 0;
            Integer scaleH = 0;

            // image height greater than screen height
//            scaleW = (int)(Math.abs(imgWidth - layoutWidth) / Math.max(imgWidth, layoutWidth));  // scale by width
//            if (imgWidth > layoutWidth) {
//                scaleW = (int)(imgWidth - (scaleW * imgWidth));
//            } else {
//                scaleW = (int)(imgHeight - (scaleW * imgHeight));
//            }
//
//            if (imgHeight> layoutHeight) {
//                scaleH = (int)(Math.abs(imgHeight - layoutHeight) / Math.max(imgHeight, layoutHeight));  // scale by height
//                scaleH = (int)(imgWidth - (scaleH * imgWidth));
//            }

            b = b.createScaledBitmap(b, this.getWidth(), this.getHeight(), true);

            canvas.drawBitmap(b, 0, 0, p);


            surfaceHolder.unlockCanvasAndPost(canvas);
        }

//        public void drawText(Canvas canvas, Paint paint, String text) {
//            Rect bounds = new Rect();
//            paint.getTextBounds(text, 0, text.length(), bounds);
//            int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
//            int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
//            canvas.drawText(text, x, y, paint);
//        }

//        public void drawText(Canvas canvas, Paint paint, String text, int y) {
//
//            Rect bounds = new Rect();
//            paint.getTextBounds(text, 0, text.length(), bounds);
//            int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
////        int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
//            canvas.drawText(text, x, y, paint);
//        }

    }

}
