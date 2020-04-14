package com.ixxhar.covid19tracker.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ixxhar.covid19tracker.R;
import com.ixxhar.covid19tracker.helperclass.CSVFileWriter;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

public class SendDataFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SendDataFragment";
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE = 1;
    private static final String MY_PREFS_NAME = "MyPrefsFile";  // This here is a constant used for Shared Preferences,
    StorageReference storageReference;
    private CSVFileWriter csv;
    private StringBuffer filePath;
    private File file;
    private NearByDeviceDBHelper nearByDeviceDBHelper;
    private Button sendDataButton;
    private String USER_ID;
    private DatabaseReference databaseReference;
    private boolean granted = false;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_senddata, container, false);

        storageReference = FirebaseStorage.getInstance().getReference();

        sendDataButton = (Button) view.findViewById(R.id.sendData_B);
        sendDataButton.setEnabled(false);
        sendDataButton.setOnClickListener(this);

        //This here is the code for getting data of logged in user
        SharedPreferences preferences = this.getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        USER_ID = preferences.getString("userid", null); //0 is the default value. //This here is the code for getting data of logged in user id

        granted = checkReadWritePermission();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(USER_ID).child("sendDataPermission").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                if (dataSnapshot.getValue().equals("true")) {
                    sendDataButton.setEnabled(true);
                } else {
                    sendDataButton.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendData_B:
                Log.d(TAG, "onClick: sendData_B");

                if (granted) {
                    if (generateCSVFileFromSQLiteDB()) {
                        uploadCSV();
                    } else {
                        Log.d(TAG, "onClick: nothing to generate csv from");
                    }
                } else {
                    Snackbar.make(view, "Need Storage Permission.",
                            Snackbar.LENGTH_LONG)
                            .show();

                    granted = checkReadWritePermission();
                }

                break;
        }
    }

    private void uploadCSV() {
        Uri uri = FileProvider.getUriForFile(view.getContext(), "com.ixxhar.covid19tracker.provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv"));

        storageReference.child("sentData" + "/" + USER_ID + ".csv").putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Snackbar.make(view, "File Uploaded Successfully.",
                            Snackbar.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "onComplete: success");
                    sendDataButton.setEnabled(false);
                    deleteGeneratedCSVFile();
                } else {
                    Log.d(TAG, "notComplete: " + task.getException());
                    Snackbar.make(view, "Filed Upload Failed.",
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

    }

    private boolean generateCSVFileFromSQLiteDB() {
        Log.d(TAG, "generateCSVFileFromSQLiteDB: ");

        deleteGeneratedCSVFile();

        nearByDeviceDBHelper = new NearByDeviceDBHelper(view.getContext());

        filePath = new StringBuffer();
        filePath.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + USER_ID + ".csv");
        file = new File(filePath.toString());

        csv = new CSVFileWriter(file);

        Cursor res = nearByDeviceDBHelper.getAllData();
        if (res.getCount() == 0) {
            // show message
            Snackbar.make(view, "No Logged Users Data..",
                    Snackbar.LENGTH_SHORT)
                    .show();
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
        if (ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_STORAGE);
            return false;
        } else {
            return true;
        }

    }

}
