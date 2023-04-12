package edu.northeastern.markergo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class EditUserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText usernameInput;
    private EditText phoneInput;
    private EditText emailInput;
    private ImageView userDP;
    private Button updateProfileBtn;

    private static int PICK_IMAGE_REQUEST_CODE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        usernameInput = findViewById(R.id.usernameInput);
        usernameInput.setText(user.getDisplayName());
        phoneInput = findViewById(R.id.phoneInput);
        phoneInput.setText(user.getProviderData().get(1).getPhoneNumber());
        emailInput = findViewById(R.id.emailInput);
        emailInput.setText(user.getProviderData().get(1).getEmail());
        userDP = findViewById(R.id.userDP);
        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);


        updateProfileBtn = findViewById(R.id.updateProfileBtn);
    }



    public void updateUserProfile(View view) {
        //update user profile
        user.updateEmail(String.valueOf(emailInput.getText()));

        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void updateUserDP(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, captureIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE);
    }

    private void setUserDP(String photoUrl) {
        if (!photoUrl.equals("null")) {
            if (user.getProviderData().get(1).getProviderId().equals("facebook.com")) {
                String token = AccessToken.getCurrentAccessToken().getToken();
                photoUrl += "?type=large&access_token=" + token;
                Log.i("url", photoUrl);
            }
            setUserDpBitmap(photoUrl);
        }
    }

    private void setUserDpBitmap(String link) {
        UrlToBitmap whatever = new UrlToBitmap(link);
        Thread thread = new Thread(whatever);
        thread.start();
        try {
            thread.join();
            Bitmap image = whatever.getImageBitmap();
            userDP.setImageBitmap(image);
            Log.i("done", "done");
        } catch (InterruptedException e) {
            Log.i("not done", "done");
            throw new RuntimeException(e);
        }
    }
}
