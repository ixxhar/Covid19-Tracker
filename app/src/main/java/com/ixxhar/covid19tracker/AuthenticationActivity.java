package com.ixxhar.covid19tracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
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

public class AuthenticationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "AuthenticationActivity";
    private static final String MY_PREFS_NAME = "MyPrefsFile";

    public static final String[] countryAreaCodes = {"93", "355", "213",
            "376", "244", "672", "54", "374", "297", "61", "43", "994", "973",
            "880", "375", "32", "501", "229", "975", "591", "387", "267", "55",
            "673", "359", "226", "95", "257", "855", "237", "1", "238", "236",
            "235", "56", "86", "61", "61", "57", "269", "242", "682", "506",
            "385", "53", "357", "420", "45", "253", "670", "593", "20", "503",
            "240", "291", "372", "251", "500", "298", "679", "358", "33",
            "689", "241", "220", "995", "49", "233", "350", "30", "299", "502",
            "224", "245", "592", "509", "504", "852", "36", "91", "62", "98",
            "964", "353", "44", "972", "39", "225", "1876", "81", "962", "7",
            "254", "686", "965", "996", "856", "371", "961", "266", "231",
            "218", "423", "370", "352", "853", "389", "261", "265", "60",
            "960", "223", "356", "692", "222", "230", "262", "52", "691",
            "373", "377", "976", "382", "212", "258", "264", "674", "977",
            "31", "687", "64", "505", "227", "234", "683", "850", "47", "968",
            "92", "680", "507", "675", "595", "51", "63", "870", "48", "351",
            "1", "974", "40", "7", "250", "590", "685", "378", "239", "966",
            "221", "381", "248", "232", "65", "421", "386", "677", "252", "27",
            "82", "34", "94", "290", "508", "249", "597", "268", "46", "41",
            "963", "886", "992", "255", "66", "228", "690", "676", "216", "90",
            "993", "688", "971", "256", "44", "380", "598", "1", "998", "678",
            "39", "58", "84", "681", "967", "260", "263"};
    RelativeLayout codePhone_RL, moreInfoSubmit_RL, otp_RL, confirmOTP_RL, authenticationActivity_RL;
    private TextInputEditText phoneET, otpET;
    private String phoneNumber, countryCode, otpNumber;
    private Button submitPhone_B, submitOTP_B, moreInfo_B;
    private TextView signininfoString_TV, otptimeout_TV;

    private String codeSent;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private AutoCompleteTextView countryCode_ACTV;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: ");

            new CountDownTimer(60 * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    Log.d(TAG, "onTick: Seconds remaining: " + millisUntilFinished / 1000);
                    otptimeout_TV.setText("Try Again in " + millisUntilFinished / 1000 + " Seconds");
                }

                public void onFinish() {
                    otptimeout_TV.setText("Tap Here to Resend OTP!");
                    otptimeout_TV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: " + phoneNumber);
                            resendVerificationCode(phoneNumber, mResendToken);
                        }
                    });
                }
            }.start();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.d(TAG, "onVerificationFailed: " + e);
            Snackbar.make(authenticationActivity_RL, "Incorrect Phone Number, Try Again!",
                    Snackbar.LENGTH_SHORT)
                    .show();

            codePhone_RL.setVisibility(View.VISIBLE);
            moreInfoSubmit_RL.setVisibility(View.VISIBLE);

            otp_RL.setVisibility(View.INVISIBLE);
            confirmOTP_RL.setVisibility(View.INVISIBLE);

            signininfoString_TV.setText("Incorrect Phone Number, Try Again");

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
            mResendToken = forceResendingToken;
        }
    };
    private UserModel userFound;
    private ProgressBar progressBar;

    private void sendVerificationode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        phoneET = (TextInputEditText) findViewById(R.id.phoneNumber_TIET);
        otpET = (TextInputEditText) findViewById(R.id.otpNumber_TIET);

        submitOTP_B = (Button) findViewById(R.id.submitOTP_B);
        submitPhone_B = (Button) findViewById(R.id.submitPhone_B);
        moreInfo_B = (Button) findViewById(R.id.moreInfo_B);

        submitOTP_B.setOnClickListener(this);
        submitPhone_B.setOnClickListener(this);
        moreInfo_B.setOnClickListener(this);

        codePhone_RL = (RelativeLayout) findViewById(R.id.codePhone_RL);
        moreInfoSubmit_RL = (RelativeLayout) findViewById(R.id.moreInfoSubmit_RL);
        otp_RL = (RelativeLayout) findViewById(R.id.otp_RL);
        confirmOTP_RL = (RelativeLayout) findViewById(R.id.confirmOTP_RL);
        authenticationActivity_RL = (RelativeLayout) findViewById(R.id.authenticationActivity_RL);

        codePhone_RL.setVisibility(View.VISIBLE);
        moreInfoSubmit_RL.setVisibility(View.VISIBLE);
        otp_RL.setVisibility(View.INVISIBLE);
        confirmOTP_RL.setVisibility(View.INVISIBLE);

        signininfoString_TV = (TextView) findViewById(R.id.signininfoString_TV);
        otptimeout_TV = (TextView) findViewById(R.id.otptimeout_TV);

        countryCode_ACTV = (AutoCompleteTextView) findViewById(R.id.countrycode_ACTV);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countryAreaCodes);
        countryCode_ACTV.setAdapter(adapter);
        countryCode_ACTV.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        countryCode_ACTV.setOnItemClickListener(this);

    }

    private void verifySignInCode(String otpNumber) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, otpNumber);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signIn WithCredential:success");

                            readDataOnce(new OnGetDataListener() {
                                @Override
                                public void onStart() {
                                    Log.d(TAG, "onStart: ");
                                }

                                @Override
                                public void onSuccess(List list) {
                                    Log.d(TAG, "onSuccess: ");

                                    showInProgress();

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

                                    startActivity(new Intent(AuthenticationActivity.this, PostLoggedInActivity.class));
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

                                Snackbar.make(authenticationActivity_RL, "Invalid OTP Entered!",
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.moreInfo_B:
                Log.d(TAG, "onClick: moreInfo_B");
                moreInfo();
                break;

            case R.id.submitPhone_B:
                Log.d(TAG, "onClick: submitPhone_B ");

                phoneNumber = String.valueOf(phoneET.getText());
                if (phoneNumber.isEmpty() || countryCode == null) {
                    phoneET.setError("Invalid Phone Number");
                    Log.d(TAG, "Invalid Phone Number");
                } else {
                    phoneNumber = "+" + countryCode + phoneNumber;
                    Log.d(TAG, "Valid Phone Number " + phoneNumber);

                    codePhone_RL.setVisibility(View.INVISIBLE);
                    moreInfoSubmit_RL.setVisibility(View.INVISIBLE);

                    otp_RL.setVisibility(View.VISIBLE);
                    confirmOTP_RL.setVisibility(View.VISIBLE);

                    signininfoString_TV.setText("Enter Received OTP " + phoneNumber);

                    sendVerificationode(phoneNumber);   //Method called for verification of phone
                }

                break;

            case R.id.submitOTP_B:
                Log.d(TAG, "onClick: submitOTP_B");

                otpNumber = String.valueOf(otpET.getText());
                if (otpNumber.isEmpty() || otpNumber.length() < 6) {
                    Log.d(TAG, "Invalid OTP Number");
                    otpET.setError("Invalid OTP Number");
                } else {
                    Log.d(TAG, "Valid OTP Number: " + otpNumber);

                    verifySignInCode(otpNumber);    //method called once recieved OTP code
                }
                break;

            default:

        }
    }

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        countryCode = parent.getItemAtPosition(position).toString();
        Log.d(TAG, "onItemClick: " + countryCode);
    }

    public interface OnGetDataListener {
        public void onStart();

        public void onSuccess(List list);

        public void onFailed(DatabaseError databaseError);
    }

    private void showInProgress() {
        progressBar = new ProgressBar(AuthenticationActivity.this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        authenticationActivity_RL.addView(progressBar, params);
        progressBar.setVisibility(View.VISIBLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void moreInfo() {
        new AlertDialog.Builder(this)
                .setTitle("More Info")
                .setMessage("Covid19-Tracker, A bluetooth proximity tracing application for keeping record of contacts, to detect and isolate corona patients.\n\nWe care about your safety and Your Data is Safe with us!")
                .create()
                .show();
    }

}
