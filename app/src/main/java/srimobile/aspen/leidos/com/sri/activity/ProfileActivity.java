package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import srimobile.aspen.leidos.com.sri.R;

/**
 * Created by walswortht on 5/5/2015.
 */
public class ProfileActivity extends Activity {



    Button profileLayout_logout_btn;
    ImageButton profileLayout_editprofile_imgBtn;
    ImageButton profileLayout_weighstationnotification_imgBtn;


    public final static int REQUEST_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        profileLayout_logout_btn                        = (Button)findViewById(R.id.profileLayout_logout_btn);
        profileLayout_editprofile_imgBtn                = (ImageButton)findViewById(R.id.profileLayout_editprofile_imgBtn);
        profileLayout_weighstationnotification_imgBtn   = (ImageButton)findViewById(R.id.profileLayout_weighstationnotification_imgBtn);

        profileLayout_logout_btn.setOnClickListener(logoutHandler);
        profileLayout_editprofile_imgBtn.setOnClickListener(editProfileHandler);
        profileLayout_weighstationnotification_imgBtn.setOnClickListener(weighStationNotificationHandler);

        // DISABLE SCREEN LOCK
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // SCREEN BRIGHTNESS
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F;
        getWindow().setAttributes(layout);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get airplaneMode (1 airplane mode is on)
        int airplaneMode = Settings.System.getInt(getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);

        if (airplaneMode == 1) {

            new AlertDialog.Builder(this)
                    .setTitle("AIRPLANE MODE IS ON")
                    .setMessage("SET AIRPLANE MODE TO OFF IN 'SETTINGS'")
                    .setPositiveButton("AIRPLANE MODE IS ON", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    View.OnClickListener logoutHandler= new View.OnClickListener() {
        public void onClick(View v) {

//            Intent explicitIntent = new Intent(ProfileActivity.this, LoginRegisterActivity.class);
//            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
        }
    };

    View.OnClickListener editProfileHandler = new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(ProfileActivity.this, TruckActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }
    };


    View.OnClickListener weighStationNotificationHandler = new View.OnClickListener() {
        public void onClick(View v) {


            Intent intent = new Intent(ProfileActivity.this, RedGreenSignalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();


        }
    };

}
