package com.locationsolutions.btconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.locationsolutions.library.Bluetooth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bluetooth b;
    }
}