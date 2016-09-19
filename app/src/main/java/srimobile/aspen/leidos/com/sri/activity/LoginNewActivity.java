package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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

import de.akquinet.android.androlog.Log;

/**
 * Created by walswortht on 5/6/2015.
 */
public class LoginNewActivity extends Activity {

    EditText loginLayout_username_edtTxt;
    EditText loginLayout_password_edtTxt;
    Button loginLayout_signin_btn;
    Button loginLayout_forgotpassword_btn;
    Button loginLayout_signup_btn;

    public static final int REQUEST_CODE = 1111111;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_new_layout);

        Log.init(this);

        loginLayout_username_edtTxt         = (EditText)findViewById(R.id.loginLayout_username_edtTxt);
        loginLayout_password_edtTxt         = (EditText)findViewById(R.id.loginLayout_password_edtTxt);
        loginLayout_signin_btn              = (Button)findViewById(R.id.loginLayout_signin_btn);
        loginLayout_forgotpassword_btn      = (Button)findViewById(R.id.loginLayout_forgotpassword_btn);
        loginLayout_signup_btn              = (Button)findViewById(R.id.loginLayout_signup_btn);

        loginLayout_signin_btn.setOnClickListener(signinHandler);
        loginLayout_forgotpassword_btn.setOnClickListener(forgotpasswordHandler);
        loginLayout_signup_btn.setOnClickListener(signupHandler);

        Log.init(this);
    }

    View.OnClickListener signinHandler = new View.OnClickListener() {
        public void onClick(View v) {

            SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();

            edit.putString("loginLayout_username_edtTxt", ((EditText) findViewById(R.id.loginLayout_username_edtTxt)).getText().toString());

            edit.commit();

            LoginSri asyncLogin = new LoginSri();
            asyncLogin.execute("http://192.168.43.8:8080/DashCon/j_security_check", "http://192.168.43.8:8080/DashCon/dashboard/");

            Intent explicitIntent = new Intent(LoginNewActivity.this, TruckActivity.class);
            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);        }
    };


    View.OnClickListener forgotpasswordHandler = new View.OnClickListener() {
        public void onClick(View v) {

            // DIALOG PASSWORD RESET EMAIL SENT
        }
    };

    View.OnClickListener signupHandler = new View.OnClickListener() {
        public void onClick(View v) {

            Intent explicitIntent = new Intent(LoginNewActivity.this, LoginRegisterActivity.class);
            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("TRUCK ACTIVITY", "ON ACTIVITY RESULT");
        Log.e("TRUCK ACTIVITY", "ON ACTIVITY RESULT");
        Log.e("TRUCK ACTIVITY", "ON ACTIVITY RESULT");

        if (requestCode == LoginNewActivity.REQUEST_CODE) {
            Log.e("TRUCK ACTIVITY", "UPDATE TRUCK IMAGE");
            Log.e("TRUCK ACTIVITY", "UPDATE TRUCK IMAGE");
            Log.e("TRUCK ACTIVITY", "UPDATE TRUCK IMAGE");
        }
    }

    private class LoginSri extends AsyncTask {

        Lock lock;

        @Override
        protected Object doInBackground(Object[] params) {

            Log.e("download", params[0].toString());

            for (Object url : params) {

                HttpPost httpPost = new HttpPost(url.toString());

                // Create a new HttpClient and Post Header
                DefaultHttpClient httpClient = HTTPClients.getDefaultHttpClient();

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("j_username", "jedi"));
                    nameValuePairs.add(new BasicNameValuePair("j_password", "password"));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    String line = "";
                    HttpResponse response = null;
                    BufferedReader reader;

                    Boolean blockingHttp = new Boolean(false);

                    // Execute HTTP Post Request
                    response = httpClient.execute(httpPost);

                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    while ((line = reader.readLine()) != null) {
                        Log.e("page", line);
                    }

                    Log.e("line", "==================" + HTTPClients.session_id);
                    Log.e("line", "==================" + HTTPClients.session_id);
                    Log.e("line", "==================" + HTTPClients.session_id);

                    Log.e("j_sec", "status->" + response.getStatusLine().toString());
                    Log.e("j_sec", "status->" + response.getStatusLine().toString());
                    Log.e("j_sec", "status->" + response.getStatusLine().toString());

                } catch (Exception e) {
                    Log.e("line", "==================" + HTTPClients.session_id);
                    Log.e("line", "==================" + HTTPClients.session_id);
                }
            }

            return "";
        }


        protected void onPostExecute(Object res) {

            Log.e("postExe", "post execute..");
            Log.e("postExe", "post execute..");
            Log.e("postExe", "post execute..");

        }
    }


}
