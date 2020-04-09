package com.ixxhar.covid19tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ixxhar.covid19tracker.modelclass.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AuthenticationActivity extends AppCompatActivity {
    private static final String TAG = "AuthenticationActivity";
    private static final String MY_PREFS_NAME = "MyPrefsFile";


    //This here is the instances of Edittext field and buttons, responsible for getting text for mobile and OTP number
    private EditText phoneET, otpET;
    private String phoneNumber, otpNumber;
    private Button submitPhone_B, submitOTP_B;
    //This here is the instances of Edittext field and buttons, responsible for getting text for mobile and OTP number

    private String codeSent;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: ");
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.d(TAG, "onVerificationFailed: " + e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            submitPhone_B.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };
    // This here is the initilization of firebase services,
    private FirebaseAuth mAuth;
    // This here is the initilization of firebase services,
    private DatabaseReference databaseReference;
    private UserModel userFound;    //This here represent user, which is searched whether it was registered before or not,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // This here is the initilization of firebase services,
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // This here is the initilization of firebase services,

        phoneET = (EditText) findViewById(R.id.phone_ET);
        otpET = (EditText) findViewById(R.id.otp_ET);
        submitOTP_B = (Button) findViewById(R.id.submitOTP_B);
        submitPhone_B = (Button) findViewById(R.id.submitPhone_B);

        submitOTP_B.setVisibility(View.INVISIBLE);
        otpET.setVisibility(View.INVISIBLE);

        //Getting values from edit text fields and calling respective function,
        submitPhone_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = String.valueOf(phoneET.getText());
                if (phoneNumber.isEmpty()) {
                    Log.d(TAG, "onClick: Empty Field");
                } else {
                    Log.d(TAG, "onClick: " + phoneNumber);

                    submitOTP_B.setVisibility(View.VISIBLE);
                    otpET.setVisibility(View.VISIBLE);
                    submitPhone_B.setVisibility(View.INVISIBLE);

                    sendVerificationode(phoneNumber);   //Method called for verification of phone
                }
            }
        });
        //Getting values from edit text fields mobile and calling respective function,

        //Getting values from edit text fields OTP and calling respective function,
        submitOTP_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpNumber = String.valueOf(otpET.getText());
                if (otpNumber.isEmpty()) {
                    Log.d(TAG, "onClick: Empty Field");
                } else {
                    Log.d(TAG, "onClick: " + otpNumber);

                    submitOTP_B.setVisibility(View.INVISIBLE);

                    verifySignInCode(otpNumber);    //method called once recieved OTP code
                }
            }
        });
        //Getting values from edit text fields OTP and calling respective function,

    }

    //Method for sending phone number for verification
    private void sendVerificationode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    //Method for sending phone number for verification

    //Method for sending OTP number for verification
    private void verifySignInCode(String otpNumber) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, otpNumber);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signIn WithCredential:success");
                            Toast.makeText(AuthenticationActivity.this, "you are logged in", Toast.LENGTH_LONG).show();

                            readDataOnce(new OnGetDataListener() {
                                @Override
                                public void onStart() {
                                    Log.d(TAG, "onStart: ");
                                }

                                @Override
                                public void onSuccess(List list) {
                                    Log.d(TAG, "onSuccess: ");

                                    //Here is a bit of a logic for checking number if it was registered before, so there is no need for creating a new unique ID
                                    for (int i = 0; i < list.size(); i++) {

                                        UserModel userModel = (UserModel) list.get(i);
                                        Log.d(TAG, "onDataChange: " + userModel.toString());

                                        if (new String(userModel.getUserPhoneNumber()).equals(phoneNumber)) {
                                            userFound = userModel;
                                        }
                                    }

                                    if (userFound != null) {
                                        Log.d(TAG, "You were here before, welcome!");
                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putString("userphone", phoneNumber);
                                        editor.putString("userid", userFound.getUserID());
                                        editor.apply();
                                    } else {
                                        DatabaseReference userReference = databaseReference.push();
                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putString("userphone", phoneNumber);
                                        editor.putString("userid", userReference.getKey());
                                        editor.apply();

                                        UserModel userModel = new UserModel();
                                        userModel.setUserID(userReference.getKey());
                                        userModel.setUserPhoneNumber(phoneNumber);
                                        userModel.setSendDataPermission("false");
                                        databaseReference.child("Users").child(userReference.getKey()).setValue(userModel);
                                    }
                                    //Here is a bit of a logic for checking number if it was registered before, so there is no need for creating a new unique ID

                                    //go to the main activity with some credentials
                                    startActivity(new Intent(AuthenticationActivity.this, BluetoothActivity.class));
                                    finish();

                                }

                                @Override
                                public void onFailed(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(AuthenticationActivity.this, "ENTER THE CORRECT CODE", Toast.LENGTH_LONG).show();
                                submitOTP_B.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
    //Method for sending OTP number for verification

    public void readDataOnce(final OnGetDataListener listener) {
        listener.onStart();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List list = new ArrayList<UserModel>();
                for (DataSnapshot dSShot : dataSnapshot.getChildren()) {
                    list.add(dSShot.getValue(UserModel.class));
                }
                listener.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface OnGetDataListener {
        public void onStart();

        public void onSuccess(List list);

        public void onFailed(DatabaseError databaseError);
    }

}
