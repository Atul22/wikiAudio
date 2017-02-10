package com.example.atul.wikiaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.atul.wikiaudio.R;
import com.example.atul.wikiaudio.rest.MediawikiClient;
import com.example.atul.wikiaudio.rest.ServiceGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartActivity extends AppCompatActivity {
    public EditText mUsername, mPassword;

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
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                if (username.isEmpty() || password.isEmpty())
                    Toast.makeText(getApplicationContext(), "Please enter your credentials!", Toast.LENGTH_LONG)
                            .show();
                else
                    initiateLogin(username, password);
            }
        });
    }

    private void initiateLogin(final String username, final String password) {
        MediawikiClient mediawikiClient = ServiceGenerator.createService(MediawikiClient.class);
        Call<ResponseBody> call = mediawikiClient.getToken("query", "tokens", "login");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        String lgToken;
                        JSONObject reader;
                        JSONObject tokenJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                            lgToken = tokenJSONObject.getString("logintoken");
                            completeLogin(username, password, lgToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Login Failed!\nPlease check your credentials/connection!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Please check your connection!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void completeLogin(final String username, final String password, final String token) {
        MediawikiClient mediawikiClient = ServiceGenerator.createService(MediawikiClient.class);
        Call<ResponseBody> call = mediawikiClient.login("login", username, password, token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject reader;
                        JSONObject loginJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            loginJSONObject = reader.getJSONObject("login");
                            String result = loginJSONObject.getString("result");
                            if (result.equals("Success")) {
                                Intent intent = new Intent(getApplicationContext(), SoundRecordingActivity.class);
                                startActivity(intent);
                            } else if (result.equals("Failed")) {
                                Toast.makeText(getApplicationContext(),
                                        loginJSONObject.getString("reason"),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Login Failed!\nPlease check your credentials/connection!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Please check your connection!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
