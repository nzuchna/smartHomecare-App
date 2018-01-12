package com.identos.smarthomecare_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String EXTRA_MESSAGE = "com.identos.smarthomecare_app";
    private static final int REQUEST_SIGNUP = 0;

    @InjectView(R.id.input_email) edtEmail _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void scanBLEDevices(View view){
            Intent intent = new Intent(this, MonitoringActivity.class);
            startActivity(intent);
    }

    public void Login(View view){
        Intent intent = new Intent(this, MonitoringActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
