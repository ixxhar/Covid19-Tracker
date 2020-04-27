package com.ixxhar.covid19tracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    private static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final String COVID19TRACKER_NEWS_CHANNEL = "covid19tracker-news";
    private SharedPreferences.Editor editor;
    private boolean isSubscribedToNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Log.d(TAG, "onCreate: ");

//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() { //this code is used for generating token for devices to send them notification, and make them specific.
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (task.isSuccessful()){
//                            String token = task.getResult().getToken();
//                            Log.d(TAG, "onComplete: "+token);
//                        }
//                    }
//                });


        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        isSubscribedToNews = prefs.getBoolean("isSubscribedToNews", false);

        if (!isSubscribedToNews) {
            FirebaseMessaging.getInstance().subscribeToTopic(COVID19TRACKER_NEWS_CHANNEL).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: isSuccessful()");
                        editor.putBoolean("isSubscribedToNews", true);
                        editor.apply();
                    } else {
                        Log.d(TAG, "onComplete: !isSuccessful()");
                        editor.putBoolean("isSubscribedToNews", false);
                        editor.apply();
                    }
                }
            });
        } else {
            Log.d(TAG, "onCreate: Already subscribed!");
        }

    }
}
