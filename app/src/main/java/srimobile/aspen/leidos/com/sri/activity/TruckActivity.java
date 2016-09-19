package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.activity.dialog.*;
import srimobile.aspen.leidos.com.sri.utils.*;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;


/**
 * Created by walswortht on 3/3/2015.
 */
public class TruckActivity extends Activity implements DialogInterface.OnDismissListener {
    ImageButton truckLay_takeTruckImage_imgBtn;

    public ToggleButton truckLay_CDL_tglBt;

    public EditText truckLay_CDL_edtTxt;
    public EditText truckLay_DL_edtTxt;
    public EditText truckLay_VIN_edtTxt;
    public EditText truckLay_USDOT_edtTxt;
    public EditText truckLay_LP_edtTxt;

    Button truckLay_saveTruckInfo_btn;
    Button truckLay_launchBT_btn;
    Button truckLay_redGreen_btn;

    int width = 0;
    int height = 0;

    public static final String MY_PREFS_NAME = "SRI";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.truck_layout);

        Intent intent = getIntent();

        truckLay_takeTruckImage_imgBtn = (ImageButton) findViewById(R.id.truckLay_takeTruckImage_imgBtn);

        truckLay_CDL_tglBt = (ToggleButton) findViewById(R.id.truckLay_CDL_tglBt);

        truckLay_CDL_edtTxt = (EditText) findViewById(R.id.truckLay_CDL_edtTxt);
        truckLay_DL_edtTxt = (EditText) findViewById(R.id.truckLay_DL_edtTxt);
        truckLay_VIN_edtTxt = (EditText) findViewById(R.id.truckLay_VIN_edtTxt);
        truckLay_USDOT_edtTxt = (EditText) findViewById(R.id.truckLay_USDOT_edtTxt);
        truckLay_LP_edtTxt = (EditText) findViewById(R.id.truckLay_LP_edtTxt);

        truckLay_saveTruckInfo_btn = (Button) findViewById(R.id.truckLay_saveTruckInfo_btn);
        truckLay_launchBT_btn = (Button) findViewById(R.id.truckLay_launchBT_btn);
        truckLay_redGreen_btn = (Button) findViewById(R.id.truckLay_redGreen_btn);

        truckLay_CDL_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_DL_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_VIN_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_USDOT_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_LP_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        truckLay_takeTruckImage_imgBtn.setOnClickListener(takePhotoFromCameraHandler);

        truckLay_saveTruckInfo_btn.setOnClickListener(saveTruckInfoHandler);
        truckLay_launchBT_btn.setOnClickListener(bTLaunchClickHandler);
        truckLay_redGreen_btn.setOnClickListener(redGreenBtnHandler);

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        // populate truck edittext form fields
        getCredPrefs();

        Log.d("TRUCKACTIVITY IMG PATH", truckImageLocation);
        Log.d("TRUCKACTIVITY IMG PATH", getApplicationContext().getPackageName());

        // if "truckV_truckImage" has not been set value of truckImageLocation would be null or empty ""
        Bitmap truckImageBitmap = null;
        try {
            if (truckImageLocation == null || truckImageLocation.trim().length() == 0) {

                truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
                truckLay_takeTruckImage_imgBtn.setImageBitmap(truckImageBitmap);

                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("truckV_truckImage", "truck_photo_placeholder_grayed");
                edit.commit();

            }
            else {
                // Now change ImageView's dimensions to match the scaled image
//                truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);
                truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation);

                if (truckImageBitmap == null) { // photo was deleted
                    truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);

                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("truckV_truckImage", "truck_photo_placeholder_grayed");
                    edit.commit();
                }
            }

            ExifInterface exif = new ExifInterface(truckImageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

            width = 600;
            height = 300;

            Bitmap resized = getScaledBitmap(truckImageBitmap, width, height);

            truckLay_takeTruckImage_imgBtn.setImageBitmap(resized);
            truckLay_takeTruckImage_imgBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);

        } catch (Exception e) {
            Log.d("EXIF", e.toString());

        }

        // disable screen lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set screen brightness
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);
    }

    private void getCredPrefs() {
        SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        truckLay_CDL_edtTxt.setText(prefs.getString("cdl", ""));
        truckLay_DL_edtTxt.setText(prefs.getString("dl", ""));
        truckLay_VIN_edtTxt.setText(prefs.getString("vin", ""));
        truckLay_USDOT_edtTxt.setText(prefs.getString("usdot", ""));
        truckLay_LP_edtTxt.setText(prefs.getString("lp", ""));
    }

    private void setCredPrefs() {

    }


    View.OnClickListener takePhotoFromCameraHandler = new View.OnClickListener() {

        public void onClick(View v) {
            try {
                FragmentManager fm = getFragmentManager();
                DialogFragment dialogFragment = new TruckImageDialogActivity();
                dialogFragment.show(fm, "SELECT TRUCK IMAGE");
            } catch (Exception e) {
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
            }
        }
    };

    View.OnClickListener saveTruckInfoHandler = new View.OnClickListener() {

        public void onClick(View view) {

            SharedPreferences.Editor edit = getSharedPreferences("SRI", Context.MODE_PRIVATE).edit();

            if (truckLay_CDL_tglBt.isChecked()) {
                edit.putString("cdl", truckLay_CDL_edtTxt.getText().toString());
                edit.putString("dl", "NOT CHECKD");
            } else{
                edit.putString("cdl", "NOT CHECKED");
                edit.putString("dl", truckLay_DL_edtTxt.getText().toString());
            }
            edit.putString("vin", truckLay_VIN_edtTxt.getText().toString());
            edit.putString("usdot", truckLay_USDOT_edtTxt.getText().toString());
            edit.putString("lp", truckLay_LP_edtTxt.getText().toString());

            edit.commit();

//            Intent explicitIntent = new Intent(TruckActivity.this, GeoFenceActivity.class);
            Intent explicitIntent = new Intent(TruckActivity.this, ProfileActivity.class);
            startActivity(explicitIntent);
        }
    };

    View.OnClickListener bTLaunchClickHandler = new View.OnClickListener() {
        public void onClick(View view) {
            Intent explicitIntent = new Intent(TruckActivity.this, SriBlutoothConnector.class);
            startActivity(explicitIntent);
        }
    };

    View.OnClickListener redGreenBtnHandler = new View.OnClickListener() {
        public void onClick(View view) {
            Intent explicitIntent = new Intent(TruckActivity.this, R.class);
            startActivity(explicitIntent);
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        if (truckImageLocation.equalsIgnoreCase("truck_photo_placeholder_grayed")) {
            Bitmap truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
            truckLay_takeTruckImage_imgBtn.setImageBitmap(truckImageBitmap);
            return;
        }

        // Now change ImageView's dimensions to match the scaled image
//        Bitmap truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);
        Bitmap truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation);

        try {
            ExifInterface exif = new ExifInterface(truckImageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);
//            truckLay_takeTruckImage_imgBtn.gets

            width = 600;
            height = 300;

            Bitmap resized = getScaledBitmap(truckImageBitmap, width, height);

            truckLay_takeTruckImage_imgBtn.setImageBitmap(resized);
            truckLay_takeTruckImage_imgBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);

        } catch (Exception e) {
            Log.d("EXIF", e.toString());
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        try {
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        if (b != null) {
            if(reqWidth < 1 && reqHeight < 1) {
                reqWidth = 100;
                reqHeight = 100;
            }

            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
        }
        return b;
    }
    public static Bitmap getRoundedCornerImage(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

    }

    public String getPath(Uri uri) {
        // Will return "image:x*"
        // getDocumentId requires api level 19
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = this.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }


}