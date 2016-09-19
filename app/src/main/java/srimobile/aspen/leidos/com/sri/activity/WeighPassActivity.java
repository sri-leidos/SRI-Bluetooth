package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import srimobile.aspen.leidos.com.sri.R;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by walswortht on 5/6/2015.
 */
public class WeighPassActivity  extends Activity {

    public static final int REQUEST_CODE = 3456;

    EditText loginLay_login_edtTxt;
    EditText loginLay_password_edtTxt;

    Button loginLay_login_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weigh_pass_layout);

    }
}
