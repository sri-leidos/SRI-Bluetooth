package srimobile.aspen.leidos.com.sri.activity.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.utils.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by walswortht on 4/13/2015.
 */
public class TruckImageDialogActivity extends DialogFragment {
    public static final int TRUCK_SD_IMAGE = 2343456;

    CustomImageView truckDiag_truckImage_imgVw;

    ImageButton truckDiag_camera_imgBtn;
    ImageButton truckDiag_folder_imgBtn;
    ImageButton truckDiag_cancel_imgBtn;
    ImageButton truckDiag_ok_imgBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.truck_photo_dialog_layout, container, false);
        getDialog().setTitle("SELECT TRUCK IMAGE");

        getDialog().setCanceledOnTouchOutside(false);

        truckDiag_truckImage_imgVw = (CustomImageView) view.findViewById(R.id.truckDiag_truckImage_imgVw);

        truckDiag_camera_imgBtn = (ImageButton) view.findViewById(R.id.truckDiag_camera_imgBtn);
        truckDiag_camera_imgBtn.setOnClickListener(getPhotoFromCamera);

        truckDiag_folder_imgBtn= (ImageButton) view.findViewById(R.id.truckDiag_folder_imgBtn);
        truckDiag_folder_imgBtn.setOnClickListener(getPhotoFromSd);

        truckDiag_cancel_imgBtn= (ImageButton) view.findViewById(R.id.truckDiag_cancel_imgBtn);
        truckDiag_cancel_imgBtn.setOnClickListener(cancel);

        truckDiag_ok_imgBtn= (ImageButton) view.findViewById(R.id.truckDiag_ok_imgBtn);
        truckDiag_ok_imgBtn.setOnClickListener(ok);

        //
        // DISPLAY CURRENT TRUCK IMAGE
        //
        SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        // Now change ImageView's dimensions to match the scaled image
        Bitmap truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);

        try {
            ExifInterface exif = new ExifInterface(truckImageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

            Bitmap resized = getScaledBitmap(truckImageBitmap, 250, 250);

            truckDiag_truckImage_imgVw.setImageBitmap(resized);
            truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } catch (Exception e) {
            Log.d("EXIF", e.toString());

            Uri path = Uri.parse("android.resource://" + view.getContext().getApplicationContext().getPackageName() + "/" + R.drawable.truck_photo_placeholder_grayed);

            truckDiag_truckImage_imgVw.setImageURI(path);
            truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        return view;
    }


    public static Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    }


    // SD PHOTO
    // SD PHOTO
    // SD PHOTO
    View.OnClickListener getPhotoFromSd = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
//                intent.setType(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); // from pictures only from the phone

                startActivityForResult(intent, TruckImageDialogActivity.TRUCK_SD_IMAGE);
            } catch (Exception e) {
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
            }
        }
    };

    // CAMERA PHOTO
    // CAMERA PHOTO
    // CAMERA PHOTO
    View.OnClickListener getPhotoFromCamera = new View.OnClickListener() {
        public void onClick(View v) {
            Intent explicitIntent = new Intent(getActivity(), TruckPhotoFromCameraActivity.class);
            startActivityForResult(explicitIntent, TruckPhotoFromCameraActivity.REQUEST_CODE);

//            Intent explicitIntent = new Intent(getActivity(), ShootAndCropActivity.class);
//            startActivityForResult(explicitIntent, ShootAndCropActivity.CAMERA_CAPTURE);
        }

    };
    // CANCEL IMAGE
    // CANCEL IMAGE
    // CANCEL IMAGE
    View.OnClickListener cancel = new View.OnClickListener() {
        public void onClick(View v) {
            dismiss();
        }
    };
    // OK KEEP IMAGE
    // OK KEEP IMAGE
    // OK KEEP IMAGE
    View.OnClickListener ok = new View.OnClickListener() {
        public void onClick(View v) {

            SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();

            edit.putString("truckV_truckImage", preferences.getString("truckV_truckImage_temp", ""));
            edit.commit();

            String truckImageLocation = preferences.getString("truckV_truckImage", "");

            Log.d("TRUCK ACTIVITY", truckImageLocation);
            Log.d("TRUCK ACTIVITY", truckImageLocation);
            Log.d("TRUCK ACTIVITY", truckImageLocation);

            dismiss();
        }
    };

    //
    // RETURN FROM INTENT
    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //
        // PHOTO FROM CAMERA INTENT FINISH
        //
        if (requestCode == TruckPhotoFromCameraActivity.REQUEST_CODE) {

            SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
            String truckImageLocation = preferences.getString("truckV_truckImage_temp", "");

            Log.d("TRUCK ACTIVITY", truckImageLocation);
            Log.d("TRUCK ACTIVITY", truckImageLocation);
            Log.d("TRUCK ACTIVITY", truckImageLocation);

//            ImageView truckDialogImage = (CustomImageView) getView().findViewById(R.id.truckDiag_truckImage_imgVw);
            Bitmap truckImageView = BitmapFactory.decodeFile(truckImageLocation);
            Bitmap resized = null;
            if (truckImageView != null) {
//                Bitmap resized = getScaledBitmap(truckImageView, 600, 300);
                resized = Bitmap.createScaledBitmap(truckImageView, 600, 300, true);
//                truckDialogImage.setImageBitmap(resized);
            }

            try {
                ExifInterface exif = new ExifInterface(truckImageLocation);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                truckImageView = rotateBitmap(resized, orientation);

//                Bitmap resized = getScaledBitmap(truckImageView, 250, 250);

                truckDiag_truckImage_imgVw.setImageBitmap(truckImageView);
                truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);

            } catch (Exception e) {
                Log.d("EXIF", e.toString());

                Uri path = Uri.parse("android.resource://" + getActivity().getApplicationContext().getPackageName() + "/" + R.drawable.truck_photo_placeholder_grayed);

                truckDiag_truckImage_imgVw.setImageURI(path);
            }

        }

        //
        // PHOTO FROM STATIC DRIVE INTENT FINISH
        //
        if (requestCode == TruckImageDialogActivity.TRUCK_SD_IMAGE) {

            if (data != null && data.getData() != null) {

                Uri selectedImageUri = data.getData();

                try {
                    String truckImageLocation = getPath(selectedImageUri);

                    SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();

                    edit.putString("truckV_truckImage_temp", truckImageLocation);
                    edit.commit();

                    Log.d("TRUCK ACTIVITY", truckImageLocation);
                    Log.d("TRUCK ACTIVITY", truckImageLocation);
                    Log.d("TRUCK ACTIVITY", truckImageLocation);

                    //
                    // DISPLAY CURRENT TRUCK IMAGE
                    //
                    Bitmap truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);

                    try {
                        ExifInterface exif = new ExifInterface(truckImageLocation);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

                        Bitmap resized = getScaledBitmap(truckImageBitmap, 250, 250);

                        truckDiag_truckImage_imgVw.setImageBitmap(resized);
                        truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    } catch (Exception e) {
                        Log.d("EXIF", e.toString());

                        Uri path = Uri.parse("android.resource://" + getActivity().getApplicationContext().getPackageName() + "/" + R.drawable.truck_photo_placeholder_grayed);

                        truckDiag_truckImage_imgVw.setImageURI(path);
                    }

                } catch (Exception e) {
                    Log.d("ExceImg", e.toString());
                    Log.d("ExceImg", e.toString());
                    Log.d("ExceImg", e.toString());
                }
            }
        }

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

        Cursor cursor = getActivity().getContentResolver().
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d("mesg", "onattch");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();

        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
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

}

