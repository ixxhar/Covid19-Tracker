package com.ixxhar.covid19tracker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ixxhar.covid19tracker.serviceclass.BluetoothLEService;
import com.ixxhar.covid19tracker.ui.HomeFragment;
import com.ixxhar.covid19tracker.ui.LoggedDataFragment;
import com.ixxhar.covid19tracker.ui.SendDataFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class PostLoggedInActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "PostLoggedInActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    TextView headPhoneNumber_TV;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private BluetoothAdapter bluetoothAdapter;  //This here is instantiation of bluetooth adapter,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postloggedin);
        Log.d(TAG, "onCreate: ");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        // This here is the initilization of firebase services,

        View headerView = navigationView.getHeaderView(0);
        headPhoneNumber_TV = (TextView) headerView.findViewById(R.id.headPhoneNumber_TV);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);

            if (currentUser != null) {

                headPhoneNumber_TV.setText(currentUser.getPhoneNumber());

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

                    //This here is the code for asking user for Location Permission
                    checkLocationPermission();

                } else {
                    if (!isServiceRunning()) {
                        Intent serviceIntent = new Intent(getApplicationContext(), BluetoothLEService.class);
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                    }

                }

            } else {
                startActivity(new Intent(this, AuthenticationActivity.class));
                finish();
            }

        }

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                Log.d(TAG, "onNavigationItemSelected: nav_home");

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_history:
                Log.d(TAG, "onNavigationItemSelected: nav_history");

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LoggedDataFragment()).commit();
                break;
            case R.id.nav_sendData:
                Log.d(TAG, "onNavigationItemSelected: nav_sendData");

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SendDataFragment()).commit();
                break;
            case R.id.nav_share:
                Log.d(TAG, "onNavigationItemSelected: nav_share");

                break;
            case R.id.nav_logout:
                Log.d(TAG, "onNavigationItemSelected: nav_logout");

                new AlertDialog.Builder(this)
                        .setTitle("Are You Sure?")
                        .setMessage("You are about to logout.")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                bluetoothAdapter.disable();
                                firebaseAuth.signOut();

                                startActivity(new Intent(PostLoggedInActivity.this, AuthenticationActivity.class));
                                finish();
                            }
                        })
                        .create()
                        .show();

                break;

        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(PostLoggedInActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        return false;
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

                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBT, REQUEST_ENABLE_BT);    //This here the code is for bluetooth initialization
                    }
                }

                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(drawerLayout, "Bluetooth Enabled",
                        Snackbar.LENGTH_LONG)
                        .show();

                Intent serviceIntent = new Intent(getApplicationContext(), BluetoothLEService.class);
                ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            }
        }
    }

    //This function check if the service is running
    private boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(BluetoothLEService.class.getName())) {
                return true;
            }
        }

        return false;
    }
}
