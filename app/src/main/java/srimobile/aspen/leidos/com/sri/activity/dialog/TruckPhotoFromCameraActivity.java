package srimobile.aspen.leidos.com.sri.activity.dialog;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;

import java.io.File;

/**
 * Created by walswortht on 4/15/2015.
 */

public class TruckPhotoFromCameraActivity extends Activity {

    public final static int REQUEST_CODE = 1234;

    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        intent = getIntent();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        Uri mImageUri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(takePictureIntent, TruckPhotoFromCameraActivity.REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TruckPhotoFromCameraActivity.REQUEST_CODE) {

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, null, null, null);
            if (cursor == null)
                return;
            // find the file in the media area
            cursor.moveToLast();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            File source = new File(filePath);
            source.getParentFile().mkdirs();

            cursor.close();

            try {
                SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();

                Log.d("IMAGE PATH", source.getCanonicalPath());
                Log.d("IMAGE PATH", source.getCanonicalPath());
                Log.d("IMAGE PATH", source.getCanonicalPath());

                edit.putString("truckV_truckImage_temp", source.getCanonicalPath());

                edit.commit();


            } catch (Exception e) {
                Log.d("TRUCK ERROR IMAGE", e.toString());
                Log.d("TRUCK ERROR IMAGE", e.toString());
                Log.d("TRUCK ERROR IMAGE", e.toString());
            }

        }

        finish();
    }
}

