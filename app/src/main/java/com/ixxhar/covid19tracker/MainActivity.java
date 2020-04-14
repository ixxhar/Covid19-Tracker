package com.ixxhar.covid19tracker;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // This here is the initilization of firebase services,
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    // This here is the initilization of firebase services,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        // This here is the initilization of firebase services,

        if (currentUser == null) {  //This if condition checkes whether the user is already registered or not, transfer them to activities accordingly
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this, PostLoggedInActivity.class));
            finish();
        }

    }
}