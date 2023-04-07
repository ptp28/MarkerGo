package edu.northeastern.markergo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView usernameTV;
    private ImageView userDP;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();

        usernameTV = findViewById(R.id.username);
        userDP = findViewById(R.id.userDP);
        email = findViewById(R.id.email);
        setUserDetails();


//        logUserDetails();
    }

    private void setUserDetails() {
        user = mAuth.getCurrentUser();
        assert user != null;

        usernameTV.setText(user.getDisplayName());
        email.setText(user.getProviderData().get(1).getEmail());
        String photoUrl = user.getPhotoUrl().toString();

        System.out.println("Email = " + user.getProviderData().get(1).getEmail());
        System.out.println("Name = " + user.getDisplayName());
    }

//    private void logUserDetails() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        Log.i("USER", user.getEmail());
//    }
}
