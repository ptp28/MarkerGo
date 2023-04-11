package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class newLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);
        getSupportActionBar().setTitle("Add new location");
    }
}