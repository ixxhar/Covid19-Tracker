package com.ixxhar.covid19tracker;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ixxhar.covid19tracker.helperclass.CSVFileWriter;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class SendDataActivity extends AppCompatActivity {
    private static final String TAG = "`SendDataActivity`";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private CSVFileWriter csv;
    private StringBuffer filePath;
    private File file;

    private NearByDeviceDBHelper nearByDeviceDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        // This here is the initilization of firebase services,

        if (currentUser != null) {

            findViewById(R.id.sendData_B).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteGeneratedCSVFile();
                    generateCSVFileFromSQLiteDB();
                    sendCSVviaEmail();

                }
            });
        } else {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }

    }

    private void sendCSVviaEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"ixxhar@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Sending Data of " + currentUser.getPhoneNumber());
        i.putExtra(Intent.EXTRA_TEXT, "This email contain data for user mentioned in the subject!");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.ixxhar.covid19tracker.provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv"));
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.setType("message/rfc822");

        startActivity(i);
    }

    private boolean generateCSVFileFromSQLiteDB() {
        Log.d(TAG, "generateCSVFileFromSQLiteDB: ");

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);

        filePath = new StringBuffer();
        filePath.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv");
        file = new File(filePath.toString());

        csv = new CSVFileWriter(file);

        Cursor res = nearByDeviceDBHelper.getAllData();
        if (res.getCount() == 0) {
            // show message
            Toast.makeText(SendDataActivity.this, "No data found in database", Toast.LENGTH_SHORT).show();
            return false;   //incase of no data found
        } else {
            csv.generateHeader();
            while (res.moveToNext()) {
                String id = res.getString(0);
                String nearByDevice = res.getString(1);
                String discoveredAt = res.getString(2);
                Log.d(TAG, "onClick: " + discoveredAt);

                csv.writeDataCSV(id, nearByDevice, discoveredAt);
            }
            return true;    //incase of CSV generated
        }
    }

    private boolean deleteGeneratedCSVFile() {
        Log.d(TAG, "deleteGeneratedCSVFile: ");

        File fdelete = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d(TAG, "file Deleted: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv");
            } else {
                Log.d(TAG, "file not Deleted: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv");
            }
            return true;
        } else {
            return false;
        }
    }
}
