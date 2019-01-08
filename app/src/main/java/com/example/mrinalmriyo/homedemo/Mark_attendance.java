package com.example.mrinalmriyo.homedemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Built by Irfan
 *
 * Attendance marking page for all non-admin users. Requires connection to hotspot named OC101 and one of the admins to be online
 * for successful attendance marking.
 *
 *
 */

public class Mark_attendance extends AppCompatActivity {

    DatabaseReference local_DBR;
    Button mark_as_present;
    TextView isOn;
    String UID;
    String formattedDate;
    int isOnline=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        Intent in = getIntent();
        UID = in.getStringExtra("uid");

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(c);

        local_DBR = FirebaseDatabase.getInstance().getReference();
        isOn = findViewById(R.id.isonlinedisplay);
        mark_as_present = findViewById(R.id.present);

        mark_as_present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline==1){
                    markPresent();
                }else{
                    Toast.makeText(Mark_attendance.this,"Attendance marking not live , pls wait ",Toast.LENGTH_LONG).show();
                }
            }
        });

        local_DBR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isOnline(local_DBR);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void markPresent(){
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        final String name = wifiInfo.getSSID();

        local_DBR.child("Attendance_Register").child(formattedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(UID) && name == "OC101"){
                    local_DBR.child("Attendance_Register").child(formattedDate).child(UID).setValue("present");
                    Toast.makeText(Mark_attendance.this,"Attendance successfully marked",Toast.LENGTH_LONG).show();
                }else if(name != "OC101"){
                    Toast.makeText(Mark_attendance.this,"Connect to OC101 to mark your attendacnce",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(Mark_attendance.this,"Your attendance has been marked for today",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isOnline(DatabaseReference loc){
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //setting attendacne marking to one account
                isOnline=dataSnapshot.child("Admin_List").child("online").getValue(Integer.class);
                if(isOnline==1){
                    isOn.setText("Mark your attendance now, connect to OC101 (PW: android) to mark your attendance");
                    isOn.setTextColor(Color.GREEN);
                }else{
                    isOn.setText("Attendance marking disabled, wait for head to come online");
                    isOn.setTextColor(Color.RED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}