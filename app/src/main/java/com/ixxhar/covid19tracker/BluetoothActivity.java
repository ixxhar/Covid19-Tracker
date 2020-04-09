package com.ixxhar.covid19tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;
import com.ixxhar.covid19tracker.serviceclass.BluetoothService;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private BluetoothAdapter bluetoothAdapter;  //This here is instantiation of bluetooth adapter,
    private NearByDeviceDBHelper nearByDeviceDBHelper;  //class responsible for creating local database

    private TextView txtDataTest;   //test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        // This here is the initilization of firebase services,

        txtDataTest = findViewById(R.id.textViewOne_TV);

        if (currentUser != null) {

            //This here is the code for asking user for Location Permission
            checkLocationPermission();

            //This here the code is for bluetooth initialization
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Phone does not support Bluetooth so let the user know and exit.
            if (bluetoothAdapter == null) {
                new AlertDialog.Builder(this)
                        .setTitle("Not compatible")
                        .setMessage("Your phone does not support Bluetooth")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            if (!bluetoothAdapter.isEnabled()) {    //check bluetooth enable
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_ENABLE_BT);    //This here the code is for bluetooth initialization
            } else {
                Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

            }

            findViewById(R.id.showSearched_B).setOnClickListener(new View.OnClickListener() {   //button to search for devices
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    showSearchedDevices();
                }
            });

            findViewById(R.id.sendDataActivityLauncher).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), SendDataActivity.class));
                }
            });

        } else {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }

    }

    //This function is responsible for testing, showing searched devices
    void showSearchedDevices() {

        Cursor res = nearByDeviceDBHelper.getAllData();
        if (res.getCount() == 0) {
            // show message
            Toast.makeText(BluetoothActivity.this, "No data found in database", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("Id :" + res.getString(0) + "\n");
            buffer.append("NearByDevice :" + res.getString(1) + "\n");
            buffer.append("DiscoveredAt :" + res.getString(2) + "\n\n");
        }
        txtDataTest.setText("");
        // Show all data
        txtDataTest.setText(buffer.toString());


    }
    //This function is responsible for updating the local DB, for logged in user, and adding child to node nearbyDevices

    //This here is an override method called upon permission requests,
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Enabled", Toast.LENGTH_SHORT).show();

                Intent serviceIntent = new Intent(getApplicationContext(), BluetoothService.class);
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    new AlertDialog.Builder(this)
                            .setTitle("Permission Denied")
                            .setMessage("Well, honestly this app is useless now!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    finish();
                                }
                            })
                            .create()
                            .show();

                }
                return;
            }

        }
    }

    //This here is an override method called upon permission requests,

    //This method is used for clearing out the bluetooth receiver
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //This method is used for clearing out the bluetooth receiver

    //The methods below from this line are those which are called on different points of activity lifecycle,
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        Log.d(TAG, "onPostResume: ");
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

    }
    //The methods below from this line are those which are called on different points of activity lifecycle,

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("In order for this application to work properly, we need the following permission.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(BluetoothActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}