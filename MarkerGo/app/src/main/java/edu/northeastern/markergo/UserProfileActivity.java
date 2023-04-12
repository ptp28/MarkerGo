package edu.northeastern.markergo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView username;
    private LinearLayout phoneLayout;
    private TextView phone;
    private ImageView userDP;
    private TextView email;
    private static int PICK_IMAGE_REQUEST_CODE = 1;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        imagesRef = storageRef.child("customUserPhotos/");

        username = findViewById(R.id.username);
        userDP = findViewById(R.id.userDP);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        phoneLayout = findViewById(R.id.phoneLayout);

        setUserDetails();
    }

    private void setUserDetails() {
        user = mAuth.getCurrentUser();
        assert user != null;

        username.setText(user.getDisplayName());
        email.setText(user.getProviderData().get(1).getEmail());
        if(user.getProviderData().get(1).getPhoneNumber() == null) {
            phoneLayout.setVisibility(View.INVISIBLE);
        } else {
            phone.setText(user.getProviderData().get(1).getPhoneNumber());
            phoneLayout.setVisibility(View.VISIBLE);
        }

        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);

        System.out.println("Provider = " + user.getProviderData());
        System.out.println("Email = " + user.getEmail());
        System.out.println("Name = " + user.getDisplayName());


//        System.out.println("Phone = " + user.getProviderData().get(1).getPhoneNumber());
//        System.out.println("Phone" + user.getProviderData().get(1).getPhoneNumber().getClass());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Image picked", Toast.LENGTH_SHORT).show();

            Uri file;
            if (data.getData() != null) {
                file = data.getData();
            } else {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                file = Uri.parse(path);
            }

            // call only if matches marker location details?
            uploadPhotoToDb(file);
        }
    }

    private void uploadPhotoToDb(Uri file) {
        StorageReference ref = imagesRef.child(file.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(file);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Log.i("status", "upload successful");
                })
                .addOnFailureListener(e -> {
                    Log.i("status", "upload failed");
                });
        uploadTask.continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return ref.getDownloadUrl();
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        updateUserProfile(uri);
                    } else {
                        Log.i("status", "failed to get download url");
                    }
                });
    }

    private void updateUserProfile(Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .build();
        user.updateProfile(profileUpdates)
                .addOnSuccessListener(unused -> setUserDpBitmap(photoUri.toString()))
                .addOnFailureListener(e -> Log.i("photoUri", "not updated"));
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


    public void startEditUserActivity(View view) {
        startActivity(new Intent(getApplicationContext(), EditUserProfileActivity.class));
    }

}
