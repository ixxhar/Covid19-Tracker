package com.ixxhar.covid19tracker;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.util.SparseArray;

import java.nio.charset.StandardCharsets;

public class DeviceUtil {
    public static final int MANUFACTURER_COVID19TRACKER = 0xABCD;   //This is our identifier
    public static final int ADVERTISEMENT_TIMEOUT = 10 * 1000;
    private static final String TAG = "DeviceUtil";

    //Check if a given record has the custom data we want
    public static boolean hasManufacturerData(ScanRecord record) {
        SparseArray<byte[]> data =
                record.getManufacturerSpecificData();

        return (data != null
                && data.get(MANUFACTURER_COVID19TRACKER) != null);
    }

    //Construct a new advertisement packet and being advertising
    public static void startAdvertising(BluetoothLeAdvertiser advertiser,
                                        AdvertiseCallback callback,
                                        String Value) {
        if (advertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(ADVERTISEMENT_TIMEOUT)    //Advertisement stops after
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                //Necessary to see friendly name in a scanning app
                .setIncludeDeviceName(false)    //not enough data to send this too, have to remove one of data element
                //Helpful for proximity calculations
                .setIncludeTxPowerLevel(true)
                //Our custom temp data
                .addManufacturerData(MANUFACTURER_COVID19TRACKER,
                        buildPayloadString(Value))
                .build();

        advertiser.startAdvertising(settings, data, callback);
    }

    //Cancel the current advertisement
    public static void stopAdvertising(BluetoothLeAdvertiser advertiser,
                                       AdvertiseCallback callback) {
        if (advertiser == null) return;

        advertiser.stopAdvertising(callback);
    }

    //Restart after a value change
    public static void restartAdvertising(BluetoothLeAdvertiser advertiser,
                                          AdvertiseCallback callback,
                                          String newValue) {
        stopAdvertising(advertiser, callback);
        startAdvertising(advertiser, callback, newValue);
    }

    //Start a new scan
    public static void startScanning(BluetoothLeScanner scanner,
                                     ScanCallback callback) {
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        scanner.startScan(null, settings, callback);
    }

    //Cancel the current scan
    public static void stopScanning(BluetoothLeScanner scanner,
                                    ScanCallback callback) {
        scanner.stopScan(callback);
    }

    /*
     * Trying to create my own payload from string
     *
     */
    private static byte[] buildPayloadString(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    /*
     * Extract the string back from the characteristic
     * payload value.
     */
    public static String unpackPayloadString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    //Utility to display raw bytes in the UI
    public static String bytesToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte chunk : data) {
            sb.append(String.format("%02X ", chunk));
        }

        return sb.toString();
    }
}