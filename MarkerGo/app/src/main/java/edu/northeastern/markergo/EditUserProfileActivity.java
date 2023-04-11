package edu.northeastern.markergo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class EditUserProfileActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // initialize input values

    }

    public void updateUserProfile(View view) {
        //update user profile

        startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
    }
}
