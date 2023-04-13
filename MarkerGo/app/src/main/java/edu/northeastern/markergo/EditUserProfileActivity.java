package edu.northeastern.markergo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class EditUserProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText usernameInput;
    private EditText emailInput;
    private ImageView userDP;
    private Button updateProfileBtn;

    private static int PICK_IMAGE_REQUEST_CODE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        usernameInput = findViewById(R.id.usernameInput);
        usernameInput.setText(user.getDisplayName());
        emailInput = findViewById(R.id.emailInput);
        emailInput.setText(user.getProviderData().get(1).getEmail());
        userDP = findViewById(R.id.userDP);
        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);


        updateProfileBtn = findViewById(R.id.updateProfileBtn);
    }

    public void updateUserProfile(View view) {
        String uid = user.getUid();
        System.out.println("UID -> " + uid);

        db.collection("users").document(uid)
                .update(
                "name", usernameInput.getText().toString(),
                "email", emailInput.getText().toString()
                ).addOnSuccessListener(task -> {
                    user.updateEmail(emailInput.getText().toString());
                    user.updateProfile(new UserProfileChangeRequest.Builder().
                            setDisplayName(usernameInput.getText().toString()).build());
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    Toast.makeText(getApplicationContext(),"User update successfully", Toast.LENGTH_SHORT).show();
                });
        //update user profile
        //


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
