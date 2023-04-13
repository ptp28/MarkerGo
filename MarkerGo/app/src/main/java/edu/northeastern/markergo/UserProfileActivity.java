package edu.northeastern.markergo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Duration;
import com.google.type.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView username;
    private TextView email;
    List<String> placesVisited;


    private TextView dateOfJoining;
    private TextView totalCheckIns;
    private TextView checkInHistory;
    private static int PICK_IMAGE_REQUEST_CODE = 1;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        imagesRef = storageRef.child("customUserPhotos/");

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        dateOfJoining = findViewById(R.id.dateOfJoining);
        totalCheckIns = findViewById(R.id.totalCheckIns);
        checkInHistory = findViewById(R.id.checkInHistory);
        checkInHistory.setText("");

        setUserDetails();
    }

    private void setUserDetails() {
        user = mAuth.getCurrentUser();
        String uid = user.getUid();
        System.out.println("UID -> " + uid);
        assert user != null;
        db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                System.out.println(documentSnapshot.get("placesVisited"));
//                setPlacesVisited((List<String>) documentSnapshot.get("placesVisited"));

                totalCheckIns.setText((String) documentSnapshot.get("count"));
            }
        });

        username.setText(user.getDisplayName());
        email.setText(user.getProviderData().get(1).getEmail());
        dateOfJoining.setText(new SimpleDateFormat("MM/dd/yyyy").format(new Date(user.getMetadata().getCreationTimestamp())));
        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);

    }

    public void setPlacesVisited(List<String> placesVisitedIDs) {
        for(String placeID: placesVisitedIDs) {
            db.collection("markers").document(placeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    checkInHistory.append(" -  " + String.valueOf(documentSnapshot.get("name")) + "\n");
                }
            });
        }
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
//            userDP.setImageBitmap(image);
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
