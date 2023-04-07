package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
                Intent intent=new Intent(MainActivity.this,landingPage.class);
                startActivity(intent);
            }));
        }
    }

    public void openLocationDetailsActivity(View view) {
        startActivity(new Intent(getApplicationContext(), LocationDetailsActivity.class));
    }

    public void openLoginActivity(View view) {
        if(mAuth.getCurrentUser() != null) {
            Toast.makeText(
                    getApplicationContext(),
                    "already logged in, signing out",
                    Toast.LENGTH_SHORT)
                    .show();
            mAuth.signOut();
        }
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}