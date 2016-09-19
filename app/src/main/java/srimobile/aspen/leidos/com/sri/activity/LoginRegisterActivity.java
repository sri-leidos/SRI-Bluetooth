package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by walswortht on 3/3/2015.
 */
public class LoginRegisterActivity extends Activity {

    public static final int REQUEST_CODE = 3456;

    EditText loginRegisterLayout_username_edtTxt;
    EditText loginRegisterLayout_email_edtTxt;
    EditText loginRegisterLayout_password_edtTxt;
    Button loginRegisterLayout_signup_btn;
    Button loginRegisterLayout_alreadyRegistered_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register_layout);

        loginRegisterLayout_username_edtTxt         = (EditText)findViewById(R.id.loginRegisterLayout_username_edtTxt);
        loginRegisterLayout_email_edtTxt            = (EditText)findViewById(R.id.loginRegisterLayout_email_edtTxt);
        loginRegisterLayout_password_edtTxt         = (EditText)findViewById(R.id.loginRegisterLayout_password_edtTxt);
        loginRegisterLayout_signup_btn              = (Button)findViewById(R.id.loginRegisterLayout_signup_btn);
        loginRegisterLayout_alreadyRegistered_btn   = (Button)findViewById(R.id.loginRegisterLayout_alreadyRegistered_btn);

        loginRegisterLayout_alreadyRegistered_btn.setOnClickListener(signOnHandler);

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
    }


    View.OnClickListener signOnHandler = new View.OnClickListener() {
        public void onClick(View v) {


            Intent explicitIntent = new Intent(LoginRegisterActivity.this, LoginNewActivity.class);
            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
        }
    };
}

