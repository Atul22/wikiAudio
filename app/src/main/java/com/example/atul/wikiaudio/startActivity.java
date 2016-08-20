package com.example.atul.wikiaudio;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class startActivity extends AppCompatActivity {

    public EditText mUsername, mPassword;
    public String usernameStr, passwordStr, responseStr;
    private Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mUsername = ( EditText ) findViewById( R.id.username );
        mPassword = ( EditText ) findViewById( R.id.password );
        submit = ( Button ) findViewById( R.id.submit );

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameStr = mUsername.getText().toString();
                passwordStr = mPassword.getText().toString();
                new LoginAsync().execute( "" );
            }
        });


    }

    private class LoginAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            if (usernameStr != null && passwordStr != null) {

                // TODO Auto-generated method stub
                responseStr = Network.login(usernameStr, passwordStr);

                // output.setText(responseStr);

            } else {
                Toast.makeText(getApplicationContext(), "Please enter all fields",
                        Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub


            super.onPostExecute(result);
            if (responseStr.equals("Success")) {
                Intent intent = new Intent(getApplicationContext(), SoundRecordingExample2.class);
                startActivity(intent);
            }else{
                Toast.makeText(getApplicationContext(), "Login Failed!\nPlease check your credentials/connection!", Toast.LENGTH_LONG).show();
            }
        }

    }
}
