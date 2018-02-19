package com.example.yqiao.myapplication;

/**
 * Created by yqiao on 2/19/18.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

    private EditText mStudyID;
    private EditText mPassword;
    private TextView mErrorField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mStudyID = (EditText) findViewById(R.id.study_id);
        mPassword = (EditText) findViewById(R.id.login_password);
        mErrorField = (TextView) findViewById(R.id.error_messages);
    }


    public void signIn(final View v){
        v.setEnabled(false);
        ParseUser.logInInBackground(mStudyID.getText().toString(), mPassword.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mErrorField.setText("Either Study ID or password are invalid.");
                    v.setEnabled(true);
                }
            }
        });
    }
}