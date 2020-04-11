package com.ixxhar.covid19tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ixxhar.covid19tracker.helperclass.CSVFileWriter;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class SendDataActivity extends AppCompatActivity {
    private static final String TAG = "`SendDataActivity`";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private CSVFileWriter csv;
    private StringBuffer filePath;
    private File file;

    private NearByDeviceDBHelper nearByDeviceDBHelper;

    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 1;

    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    private Button sendDataButton;
    private String USER_ID;
    private DatabaseReference databaseReference;
    StorageReference storageReference;

    private boolean granted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        // This here is the initilization of firebase services,
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        // This here is the initilization of firebase services,

        sendDataButton = (Button) findViewById(R.id.sendData_B);

        //This here is the code for getting data of logged in user
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        USER_ID = prefs.getString("userid", null); //0 is the default value. //This here is the code for getting data of logged in user id

        if (currentUser != null) {

            granted = checkReadWritePermission();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("Users").child(USER_ID).child("sendDataPermission").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                    if (dataSnapshot.getValue().equals("true")) {
                        sendDataButton.setEnabled(true);
                        sendDataButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (granted) {
                                    if (generateCSVFileFromSQLiteDB()) {
                                        uploadCSV();
                                    } else {
                                        Log.d(TAG, "onClick: nothing to generate csv from");
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Grant Permission", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    } else {
                        sendDataButton.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }

    }

    private void uploadCSV() {
//        Intent i = new Intent(Intent.ACTION_SEND);
//        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"ixxhar@gmail.com"});
//        i.putExtra(Intent.EXTRA_SUBJECT, "Sending Data of " + currentUser.getPhoneNumber());
//        i.putExtra(Intent.EXTRA_TEXT, "This email contain data for user mentioned in the subject!");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.ixxhar.covid19tracker.provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv"));
//        i.putExtra(Intent.EXTRA_STREAM, uri);
//        i.setType("message/rfc822");
//
//        startActivity(i);


        storageReference.child("sentData" + "/" + USER_ID + ".csv").putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Data Sent Successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: success");
                    sendDataButton.setEnabled(false);
                } else {
                    Log.d(TAG, "notComplete: " + task.getException());
                }
            }
        });

    }

    private boolean generateCSVFileFromSQLiteDB() {
        Log.d(TAG, "generateCSVFileFromSQLiteDB: ");

        deleteGeneratedCSVFile();

        nearByDeviceDBHelper = new NearByDeviceDBHelper(this);

        filePath = new StringBuffer();
        filePath.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv");
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

        File fdelete = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv");
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d(TAG, "file Deleted: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv");
            } else {
                Log.d(TAG, "file not Deleted: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv");
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean checkReadWritePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

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
                                ActivityCompat.requestPermissions(SendDataActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE);
            }
            return false;
        } else {
            return true;
        }

    }
}
