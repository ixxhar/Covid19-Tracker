package com.ixxhar.covid19tracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class TestingActivity extends AppCompatActivity {
    private static final String TAG = "TestingActivity";

    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    private EditText editTextOne;
    private TextView textViewOne;

    private DatabaseReference databaseReference;
    private Button buttonOne;
    private String USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        editTextOne = (EditText) findViewById(R.id.editTextOne_ET);
        textViewOne = (TextView) findViewById(R.id.textViewOne_TV);
        buttonOne = (Button) findViewById(R.id.one_B);

        buttonOne.setVisibility(View.INVISIBLE);

        //This here is the code for getting data of logged in user
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        USER_ID = prefs.getString("userid", null); //0 is the default value. //This here is the code for getting data of logged in user id

        Log.d(TAG, "onCreate: " + USER_ID);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(USER_ID).child("sendDataPermission").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                if (dataSnapshot.getValue().toString() == "true") {
                    buttonOne.setVisibility(View.VISIBLE);
                } else {
                    buttonOne.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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