package edu.northeastern.markergo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

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

        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);

        System.out.println("Email = " + user.getProviderData().get(1).getEmail());
        System.out.println("Name = " + user.getDisplayName());
    }

    private void setUserDP(String photoUrl) {
        if (!photoUrl.equals("null")) {
            if (user.getProviderData().get(1).getProviderId().equals("facebook.com")) {
                String token = AccessToken.getCurrentAccessToken().getToken();
                photoUrl += "?type=large&access_token=" + token;
                Log.i("url", photoUrl);
            }
            Thread setUserDP = new Thread(new UrlToBitmap(photoUrl));
            setUserDP.start();
        }
    }

    private class UrlToBitmap implements Runnable {
        URL url;

        UrlToBitmap(String photoUrl) {
            try {
                this.url = new URL(photoUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.i("idk", "on thread");
            try {
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> userDP.setImageBitmap(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private void logUserDetails() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        Log.i("USER", user.getEmail());
//    }
}
