package com.example.atul.wikiaudio.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atul.wikiaudio.R;
import com.example.atul.wikiaudio.rest.MediaWikiClient;
import com.example.atul.wikiaudio.rest.ServiceGenerator;
import com.example.atul.wikiaudio.util.WAVRecorder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoundRecordingActivity extends AppCompatActivity {

    private static final String TAG = SoundRecordingActivity.class.getSimpleName();

    private static final String RECORDED_FILENAME = "record.wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    final WAVRecorder recorder = new WAVRecorder();
    public Boolean mStartPlaying = true;
    private Button playButton;
    private TextView recordText;
    private MediaPlayer mPlayer = null;

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button recordButton = (Button) findViewById(R.id.btnStart);
        playButton = (Button) findViewById(R.id.btnPlay);
        recordText = (TextView) findViewById(R.id.space);
        Button uploadButton = (Button) findViewById(R.id.upload_button);


        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "Start Recording");
                    stopPlaying();
                    playButton.setText(R.string.play_button_start);
                    recorder.startRecording();
                    recordText.setText(R.string.now_recording);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "Stop Recording");
                    recordText.setText("");
                    mStartPlaying = true;
                    recorder.stopRecording(getFilename());
                }
                return false;
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayStatusChanged();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateUpload("record_test.wav", getFilename());
            }
        });
    }

    private void initiateUpload(final String title, final String filepath) {
        MediaWikiClient mediaWikiClient = ServiceGenerator.createService(MediaWikiClient.class,
                getApplicationContext());
        Call<ResponseBody> call = mediaWikiClient.getToken("query", "tokens", null);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseStr = response.body().string();
                        String editToken;
                        JSONObject reader;
                        JSONObject tokenJSONObject;
                        try {
                            reader = new JSONObject(responseStr);
                            tokenJSONObject = reader.getJSONObject("query").getJSONObject("tokens");
                            //noinspection SpellCheckingInspection
                            editToken = tokenJSONObject.getString("csrftoken");
                            if (editToken.equals("+\\")) {
                                Toast.makeText(getApplicationContext(),
                                        "You are not logged in! \nPlease login to continue.",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(),
                                        LoginActivity.class);
                                startActivity(intent);
                            } else {
                                completeUpload(title, filepath, editToken);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Server misbehaved! \nPlease try again later.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Please check your connection!",
                                Toast.LENGTH_LONG).show();
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

    private void completeUpload(String title, String filePath, String editToken) {
        // create upload service client
        MediaWikiClient service =
                ServiceGenerator.createService(MediaWikiClient.class, getApplicationContext());

        File file = new File(filePath);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getMimeType(filePath)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", title, requestFile);

        // finally, execute the request
        Call<ResponseBody> call = service.uploadFile(
                RequestBody.create(MultipartBody.FORM, "upload"),
                RequestBody.create(MultipartBody.FORM, title),
                RequestBody.create(MultipartBody.FORM, editToken),
                body,
                RequestBody.create(MultipartBody.FORM, "{{PD-self}}")
        );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                try {
                    String responseStr = response.body().string();
                    JSONObject reader;
                    JSONObject uploadJSONObject;
                    try {
                        reader = new JSONObject(responseStr);
                        uploadJSONObject = reader.getJSONObject("upload");
                        String result = uploadJSONObject.getString("result");
                        Toast.makeText(getApplicationContext(),
                                "Upload: " + result,
                                Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Server misbehaved! \nPlease try again later.",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Please check your connection!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Please check your connection!",
                        Toast.LENGTH_LONG).show();
                Log.e("Upload error:", t.getMessage());
            }
        });
    }

    private void onPlayStatusChanged() {
        if (mStartPlaying)
            startPlaying();
        else
            stopPlaying();
        if (mStartPlaying) {
            playButton.setText(R.string.play_button_stop);
        } else {
            playButton.setText(R.string.play_button_start);
        }
        mStartPlaying = !mStartPlaying;
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getFilename());
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onPlayStatusChanged();
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("error", "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d(TAG, "Can not create directory!");
        }

        return file.getAbsolutePath() + "/" + RECORDED_FILENAME;
    }
}