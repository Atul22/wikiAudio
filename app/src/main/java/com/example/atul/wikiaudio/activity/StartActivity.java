package com.example.atul.wikiaudio.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.atul.wikiaudio.Network;
import com.example.atul.wikiaudio.R;

public class StartActivity extends AppCompatActivity {
    public EditText mUsername, mPassword;
    public String usernameStr, passwordStr, responseStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        Button submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameStr = mUsername.getText().toString();
                passwordStr = mPassword.getText().toString();
                new LoginAsync().execute("");
            }
        });
    }

    private class LoginAsync extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            if (usernameStr != null && passwordStr != null) {
                responseStr = Network.login(usernameStr, passwordStr);
            } else {
                Toast.makeText(getApplicationContext(), "Please enter all fields",
                        Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (responseStr.equals("Success")) {
                Intent intent = new Intent(getApplicationContext(), SoundRecordingActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Login Failed!\nPlease check your credentials/connection!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
