package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button mapsButton = findViewById(R.id.buttonMapsActivity);
        if (mapsButton != null) {
            mapsButton.setOnClickListener((it -> {
                Intent intent = new Intent(MainActivity.this, landingPage.class);
                startActivity(intent);
            }));
        }
    }
}