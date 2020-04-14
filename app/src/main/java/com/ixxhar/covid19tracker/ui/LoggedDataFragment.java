package com.ixxhar.covid19tracker.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.ixxhar.covid19tracker.R;
import com.ixxhar.covid19tracker.helperclass.NearByDeviceDBHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoggedDataFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoggedDataFragment";

    Button showLoggedData_B;
    TextView loggedusers_TV;

    private NearByDeviceDBHelper nearByDeviceDBHelper;  //class responsible for creating local database
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_loggeddata, container, false);
        nearByDeviceDBHelper = new NearByDeviceDBHelper(view.getContext());

        showLoggedData_B = (Button) view.findViewById(R.id.showLoggedData_B);
        loggedusers_TV = (TextView) view.findViewById(R.id.loggedusers_TV);
        loggedusers_TV.setMovementMethod(new ScrollingMovementMethod());

        showLoggedData_B.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showLoggedData_B:
                Log.d(TAG, "onClick: showLoggedData_B");
                showSearchedDevices();

                break;
        }
    }

    //This function is responsible for testing, showing searched devices
    private void showSearchedDevices() {

        Cursor res = nearByDeviceDBHelper.getAllData();
        if (res.getCount() == 0) {
            Snackbar.make(view, "No Users Found.",
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append("Id :" + res.getString(0) + "\n");
            buffer.append("NearByDevice :" + res.getString(1) + "\n");
            buffer.append("DiscoveredAt :" + res.getString(2) + "\n\n");
        }
        loggedusers_TV.setText("");
        // Show all data
        loggedusers_TV.setText(buffer.toString());


    }
}
