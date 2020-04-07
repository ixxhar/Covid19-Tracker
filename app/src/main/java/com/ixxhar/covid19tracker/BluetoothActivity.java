package com.ixxhar.covid19tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.ixxhar.covid19tracker.modelclass.DeviceModel;
import com.ixxhar.covid19tracker.modelclass.UserModel;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    //This here are the constants for permission dialog,
    private static final int REQUEST_ENABLE_BT = 1;                         //Enable bluetooth request variable
    private static final int REQUEST_DISCOVERABILITY = 1;                  //Make bluetooth discoverable request variable
    // This here is the initilization of firebase services,
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    ArrayList<DeviceModel> nearbyDeviceModelArrayList;  //This here is an array which is used for storing nearby devices that are found by Bluetooth.
    UserModel currentUserModel; //This here is the instantiation of user model class, which is used from the logged in user data.
    // This here is the initilization of firebase services,
    private FirebaseAuth firebaseAuth;
    //This here are the constants for permission dialog,
    private FirebaseUser currentUser;
    private BluetoothAdapter bluetoothAdapter;  //This here is instantiation of bluetooth adapter,
    private NearByDeviceDBHelper nearByDeviceDBHelper;  //class responsible for creating local database

    private TextView txtDataTest;   //test
    //This method is all about bluetooth, here it is filtering out devices whose type is 2, and assigning them to the array mentioned above, and calling a method to update firebase realtime db
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals((action))) {    //This check the intent whether it has found a device or not
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceModel deviceModel;
                Log.d(TAG, "ACTION_FOUND: " + device.getName() + " " + device.getAddress() + " " + device.getType());

                if (device.getName() != null && device.getName().startsWith("-")) {
                    deviceModel = new DeviceModel();
                    deviceModel.setDeviceID(device.getName());
                    deviceModel.setLoggedTime(String.valueOf(Calendar.getInstance().getTime()));
                    nearbyDeviceModelArrayList.add(deviceModel);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED " + nearbyDeviceModelArrayList.size()); //This checks whether the scanning is finished.
                updateNearbyList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        // This here is the initilization of firebase services,

        txtDataTest = findViewById(R.id.txtData);

        if (currentUser != null) {

            //This here is the code for getting data of logged in user
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String userphone = prefs.getString("userphone", null);//"No name defined" is the default value.
            String userid = prefs.getString("userid", null); //0 is the default value.
            //This here is the code for getting data of logged in user

            Log.d(TAG, "onCreate: userphone " + userphone + " userid " + userid); // getting logged in user data.

            //This here is the instantiation of user model class, which is used from the logged in user data.
            currentUserModel = new UserModel();
            currentUserModel.setUserID(userid);
            currentUserModel.setUserPhoneNumber(userphone);
            //This here is the instantiation of user model class, which is used from the logged in user data.

            //This here is the code for asking user for Location Permission
            checkLocationPermission();

            //This here is the code for asking user for Location Permission

            //This here the code is for bluetooth initialization
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.setName(currentUserModel.getUserID());   //here I have to assign a unique identifier to be linked with the number

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
                startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            }
            //This here the code is for bluetooth initialization

            findViewById(R.id.showSearched_B).setOnClickListener(new View.OnClickListener() {   //button to search for devices
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: ");

                    //This here code is for filtering out the activities done by a device bluetooth
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    registerReceiver(broadcastReceiver, filter);
                    //This here code is for filtering out the activities done by a device bluetooth

                    nearbyDeviceModelArrayList = new ArrayList<DeviceModel>();  //An array for storing found devices

                    bluetoothAdapter.setName(currentUserModel.getUserID());   //here I have to assign a unique identifier to be linked with the number
                    bluetoothAdapter.startDiscovery();  //This function is self explanatory, it basically start the intent which we describe above
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
    //This method is all about bluetooh, here it is filtering out devices whose type is 2, and assigning them to the array mentioned above, and calling a method to update firebase realtime db

    //This function is responsible for updating the local DB, for logged in user, and adding child to node nearbyDevices
    void updateNearbyList() {

        for (DeviceModel deviceModel : nearbyDeviceModelArrayList) {
            Log.d(TAG, ": " + deviceModel.toString());
            try {
                boolean isInserted = nearByDeviceDBHelper.insertData(deviceModel.getDeviceID(), deviceModel.getLoggedTime());
                if (isInserted == true)
                    Log.d(TAG, "updateNearbyList: Data Inserted");
                else {
                    Log.d(TAG, "updateNearbyList: Data not Inserted");
                }
            } catch (Exception ex) {
                Log.d(TAG, "updateNearbyList: " + ex.getMessage());
            }
        }

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
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //This method is used for clearing out the bluetooth receiver

    //The methods below from this line are those which are called on different points of activity lifecycle, here it is used for keeping the device a unique
    @Override
    protected void onStop() {
        super.onStop();
        bluetoothAdapter.setName(currentUserModel.getUserID());   //here I have to assign a unique identifier to be linked with the number
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        bluetoothAdapter.setName(currentUserModel.getUserID());   //here I have to assign a unique identifier to be linked with the number
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothAdapter.setName(currentUserModel.getUserID());   //here I have to assign a unique identifier to be linked with the number
    }
    //The methods below from this line are those which are called on different points of activity lifecycle, here it is used for keeping the device a unique

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