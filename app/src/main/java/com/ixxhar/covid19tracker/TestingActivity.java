package com.ixxhar.covid19tracker;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ixxhar.covid19tracker.helperclass.CSVFileWriter;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;
import com.ixxhar.covid19tracker.modelclass.UserModel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class TestingActivity extends AppCompatActivity {
    private static final String TAG = "TestingActivity";

    EditText editText;
    UserModel userFound;
    UserModel userFound1;
    List<UserModel> userList;
    String phoneNumber;
    boolean isExist = false;
    private TextView txtData;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private NearByDeviceDBHelper myDb;

    CSVFileWriter csv;
    StringBuffer filePath;
    File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        myDb = new NearByDeviceDBHelper(this);

        editText = (EditText) findViewById(R.id.testPhone_ET);
        txtData = findViewById(R.id.txtData);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        userFound = new UserModel();
        userFound.setUserID(currentUser.getUid());
        userFound.setUserPhoneNumber(currentUser.getPhoneNumber());

        filePath = new StringBuffer();
        filePath.append(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.csv");
        file = new File(filePath.toString());

        csv = new CSVFileWriter(file);

        findViewById(R.id.testSubmit_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                phoneNumber = String.valueOf(editText.getText());

                try {
                    Date currentTime = Calendar.getInstance().getTime();

                    boolean isInserted = myDb.insertData("FFxkkdlsile", String.valueOf(currentTime));
                    if (isInserted == true)
                        Toast.makeText(TestingActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                    else {
                        Toast.makeText(TestingActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(TestingActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.sendEmail_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Generating CSV");
                Cursor res = myDb.getAllData();
                if (res.getCount() == 0) {
                    // show message
                    Toast.makeText(TestingActivity.this, "No data found in database", Toast.LENGTH_SHORT).show();
                    return;
                }

                csv.generateHeader();
                while (res.moveToNext()) {
                    String id = res.getString(0);
                    String nearByDevice = res.getString(1);
                    String discoveredAt = res.getString(2);
                    Log.d(TAG, "onClick: " + discoveredAt);

                    csv.writeDataCSV(id, nearByDevice, discoveredAt);
                }

//                Log.d(TAG, "onClick: delete");
//                File fdelete = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/test.csv");
//                if (fdelete.exists()) {
//                    if (fdelete.delete()) {
//                        System.out.println("file Deleted :" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/test.csv");
//                    } else {
//                        System.out.println("file not Deleted :" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/test.csv");
//                    }
//                }

            }
        });

        findViewById(R.id.get_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = myDb.getAllData();
                if (res.getCount() == 0) {
                    // show message
                    Toast.makeText(TestingActivity.this, "No data found in database", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()) {
                    buffer.append("Id :" + res.getString(0) + "\n");
                    buffer.append("NearByDevice :" + res.getString(1) + "\n");
                    buffer.append("DiscoveredAt :" + res.getString(2) + "\n\n");
                }
                txtData.setText("");
                // Show all data
                txtData.setText(buffer.toString());

            }
        });

    }

}