package com.example.atul.wikiaudio;

import android.util.Log;

/**
 * Created by atul on 8/7/16.
 */
public class AppLog {

    private static final String APP_TAG = "WikiAudio";

    public static int logString(String message) {
        return Log.i(APP_TAG, message);
    }
}
