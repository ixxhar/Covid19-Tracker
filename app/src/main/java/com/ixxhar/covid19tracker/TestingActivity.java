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
import com.ixxhar.covid19tracker.helperclass.AESEncryptionHelper;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;
import com.ixxhar.covid19tracker.modelclass.UserModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
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

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

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

        findViewById(R.id.testSubmit_B).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                phoneNumber = String.valueOf(editText.getText());

                try {
                    String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                    //you can encrypt any text of your choice. I am encrypting phone number using "izhar"
                    String encPhoneNumber = new AESEncryptionHelper("izhar").encrypt("03349499395");    //we are talking about identifier what we need to do, is encrypt phone number in firebase
                    Log.d(TAG, "onClick: " + encPhoneNumber);

                    boolean isInserted = myDb.insertData("FFxkkdlsile", currentDateTime);
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
                String dbname = myDb.getDatabaseName();
                String dbpath = getApplication().getDatabasePath(dbname).getPath();
                Log.d(TAG, "onClick: " + dbpath);

                try {
                    copyFile(getApplication().getDatabasePath(dbname), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/test.db"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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