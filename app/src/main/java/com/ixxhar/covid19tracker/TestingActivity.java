package com.ixxhar.covid19tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.ixxhar.covid19tracker.serviceclass.BluetoothLEService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TestingActivity extends AppCompatActivity {
    private static final String TAG = "TestingActivity";

    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    private EditText editTextOne;
    private TextView textViewOne;

    private DatabaseReference databaseReference;
    private String USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        editTextOne = (EditText) findViewById(R.id.editTextOne_ET);
        textViewOne = (TextView) findViewById(R.id.textViewOne_TV);

        //This here is the code for getting data of logged in user
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        USER_ID = prefs.getString("userid", null); //0 is the default value. //This here is the code for getting data of logged in user id

        Log.d(TAG, "onCreate: " + USER_ID);

        findViewById(R.id.one_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent serviceIntent = new Intent(getApplicationContext(), BluetoothLEService.class);
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

            }
        });

        findViewById(R.id.two_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.three_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}