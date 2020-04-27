package com.ixxhar.covid19tracker.serviceclass;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.ixxhar.covid19tracker.DeviceUtil;
import com.ixxhar.covid19tracker.MainActivity;
import com.ixxhar.covid19tracker.R;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.ixxhar.covid19tracker.App.CHANNEL_ID;

public class BluetoothLEService extends Service {
    public static final int SCANNING_TIMEOUT = 10 * 1000;
    public static final int BROADCASTING_PERIOD = 10 * 60 * 1000;   //10 Minutes
    private static final String TAG = "BluetoothLEService";
    private static final String MY_PREFS_NAME = "MyPrefsFile";  //This here is a constant used for Shared Preferences,
    private String USER_ID;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothLeScanner mBluetoothLeScanner;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private NearByDeviceDBHelper nearByDeviceDBHelper;  //class responsible for creating local database
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            final ScanRecord record = result.getScanRecord();
            if (!DeviceUtil.hasManufacturerData(record)) {
                Log.d(TAG, "This is the device we're looking for...");
                return;
            }

            //Post scan update to the DB
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "This is the device we're looking for...");
                    updateScanResult(record);
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "Error scanning devices: " + errorCode);
        }
    };
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);  //initializing DB

        //This here is the code for getting data of logged in user
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        //This here is the code for getting data of logged in user
        USER_ID = prefs.getString("userid", null); //0 is the default value.

        Log.d(TAG, "onCreate:" + " USER_ID " + USER_ID);

        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        if (mBluetoothAdapter != null) {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (mBluetoothAdapter.isEnabled()) {
                //Bluetooth is enabled

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

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


                    //Bluetooth has FEATURE_BLUETOOTH_LE
                    startScanningAdvertising();

                }
            } else {
                bluetoothNotEnabledNotification();
            }

        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startScanningAdvertising() {   //custom method for scanning timeout
        DeviceUtil.startAdvertising(mBluetoothLeAdvertiser, mAdvertiseCallback, USER_ID);    //this is where advertisement is started
        DeviceUtil.startScanning(mBluetoothLeScanner, mScanCallback);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // stop scanning here
                try {

                    if (mBluetoothAdapter.isEnabled()) {
                        DeviceUtil.stopScanning(mBluetoothLeScanner, mScanCallback);
                        DeviceUtil.stopAdvertising(mBluetoothLeAdvertiser, mAdvertiseCallback);
                    } else {
                        bluetoothNotEnabledNotification();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                new CountDownTimer(BROADCASTING_PERIOD, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if (mBluetoothAdapter.isEnabled()) {
                            Log.d(TAG, "onTick: Seconds remaining: " + millisUntilFinished / 1000);
                        } else {
                            bluetoothNotEnabledNotification();
                            return;
                        }
                    }

                    public void onFinish() {
                        if (mBluetoothAdapter.isEnabled()) {
                            startScanningAdvertising();
                        } else {
                            bluetoothNotEnabledNotification();
                        }
                    }
                }.start();

                Log.d(TAG, "run: Stopped");
            }
        }, SCANNING_TIMEOUT);
    }

    private void bluetoothNotEnabledNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Permission")
                .setContentText("Enable Bluetooth, Try Again!")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        stopSelf();
    }

    private void updateScanResult(ScanRecord record) {  //This method is used for storing found packet data in local DB.

        //Extract our custom temp value
        byte[] customData = record.getManufacturerSpecificData(DeviceUtil.MANUFACTURER_COVID19TRACKER);

        String tempID = DeviceUtil.unpackPayloadString(customData);
        String userID = tempID;

        Log.d(TAG, "updateScanResult: " + userID);

        if (userID.startsWith("-")) {   //just in case
            try {
                boolean isInserted = nearByDeviceDBHelper.insertData(userID, String.valueOf(Calendar.getInstance().getTime()));
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
