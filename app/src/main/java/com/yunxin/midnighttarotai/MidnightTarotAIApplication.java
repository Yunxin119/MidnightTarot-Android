package com.yunxin.midnighttarotai;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import android.util.Log;

/**
 * Custom Application class for MidnightTarot
 * Handles application-level initialization
 *
 * @author Yunxin
 * @version 2.0
 */
public class MidnightTarotAIApplication extends Application {
    private static final String TAG = "MidnightTarotApp";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFirebase();
    }

    /**
     * Initializes Firebase components
     */
    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}