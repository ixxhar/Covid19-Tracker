package com.ixxhar.covid19tracker.serviceclass;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.ixxhar.covid19tracker.MainActivity;
import com.ixxhar.covid19tracker.R;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;
import com.ixxhar.covid19tracker.modelclass.DeviceModel;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.ixxhar.covid19tracker.App.CHANNEL_ID;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    ArrayList<DeviceModel> nearbyDeviceModelArrayList;  //This here is an array which is used for storing nearby devices that are found by Bluetooth.
    private BluetoothAdapter bluetoothAdapter;  //This here is instantiation of bluetooth adapter,
    private NearByDeviceDBHelper nearByDeviceDBHelper;  //class responsible for creating local database
    //This method is all about bluetooth, here it is filtering out devices whose we assigned IDs, and assigning them to the array mentioned above, and calling a method to update firebase realtime db
    private String bluetoothOriginalName;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals((action))) {    //This check the intent whether it has found a device or not
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceModel deviceModel;
                Log.d(TAG, "ACTION_FOUND: " + device.getName() + " " + device.getAddress() + " " + device.getType());

                if (device.getName() != null && device.getName().startsWith("-")) {
                    nearbyDeviceModelArrayList = new ArrayList<DeviceModel>();  //An array for storing found devices

                    deviceModel = new DeviceModel();
                    deviceModel.setDeviceID(device.getName());
                    deviceModel.setLoggedTime(String.valueOf(Calendar.getInstance().getTime()));
                    nearbyDeviceModelArrayList.add(deviceModel);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED " + nearbyDeviceModelArrayList.size()); //This checks whether the scanning is finished.
                updateNearbyList();

                new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        Log.d(TAG, "onTick: Seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        if (bluetoothAdapter.isEnabled()) {
                            bluetoothAdapter.startDiscovery();
                        } else {
                            bluetoothNotEnabledNotification();
                        }
                    }
                }.start();

            }
        }
    };
    private String USER_ID;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);  //initializing DB

        //This here the code is for bluetooth initialization
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Assign original name of bluetooth
        bluetoothOriginalName = bluetoothAdapter.getName();

        //This here is the code for getting data of logged in user
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        USER_ID = prefs.getString("userid", null); //0 is the default value.
        //This here is the code for getting data of logged in user

        Log.d(TAG, "onCreate:" + " USER_ID " + USER_ID); // getting logged in user data.


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {    //check bluetooth enable

                bluetoothNotEnabledNotification();

            } else {
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Service Active")
                        .setContentText("Searching for Nearby Users")
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(2, notification);

                //do heavy work on a background thread

                //This here code is for filtering out the activities done by a device bluetooth
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(broadcastReceiver, filter);
                //This here code is for filtering out the activities done by a device bluetooth

                bluetoothAdapter.setName(USER_ID);   //here I have to assign a unique identifier to be linked with the number

                bluetoothAdapter.startDiscovery();  //This function is self explanatory, it basically start the intent which we describe above

                //stopSelf();
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        try {
            bluetoothAdapter.setName(bluetoothOriginalName);
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    private void bluetoothNotEnabledNotification() {
        bluetoothAdapter.setName(bluetoothOriginalName);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)    //for some reason it does not display what I want
                .setContentTitle("Bluetooth Permission")
                .setContentText("Enable Bluetooth, Try Again!")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        stopSelf();
    }
    //This function is responsible for updating the local DB, for logged in user, and adding child to node nearbyDevices

    //This function is responsible for updating the local DB, for logged in user, and adding child to node nearbyDevices
    private void updateNearbyList() {
        Log.d(TAG, "updateNearbyList: " + nearbyDeviceModelArrayList.size());

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
    }

}