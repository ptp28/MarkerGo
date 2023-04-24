package edu.northeastern.markergo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.markergo.models.CheckInHistory;
import edu.northeastern.markergo.utils.UrlToBitmap;

public class UserProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView username;
    private TextView email;
    private ImageView userDP;
    List<CheckInHistory> checkInList = new ArrayList<>();


    private TextView dateOfJoining;
    private TextView totalCheckIns;
    private TextView points;
    private TextView checkInHistory;
    private static int PICK_IMAGE_REQUEST_CODE = 1;
    private static int UPDATE_PROFILE_REQUEST_CODE = 2;
    private StorageReference storageRef;
    private StorageReference imagesRef;
    private DocumentReference userRef;
    private RecyclerView checkInHistoryRV;
    CheckInRecyclerViewAdapter checkInAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        imagesRef = storageRef.child("customUserPhotos/");
        userRef = db.collection("users").document(user.getUid());

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        userDP = findViewById(R.id.userDP);
        dateOfJoining = findViewById(R.id.dateOfJoining);
        totalCheckIns = findViewById(R.id.totalCheckIns);
        points = findViewById(R.id.points);

        checkInAdapter = new CheckInRecyclerViewAdapter(checkInList);
        checkInHistoryRV = findViewById(R.id.checkInHistoryRV);
        checkInHistoryRV.setLayoutManager(new LinearLayoutManager(this));
        checkInHistoryRV.setAdapter(checkInAdapter);

        checkInHistory = findViewById(R.id.checkInHistory);
        checkInHistory.setText("");
        totalCheckIns.setText("0");
        setUserDetails();

    }

    private void setUserDetails() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            if (data == null || !data.containsKey("points")) {
                points.setText("0");
            } else {
                points.setText(String.valueOf(data.get("points")));
            }
        });

        userRef.collection("placesVisited")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    final int[] totalCount = {0};
                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        setPlacesVisitedText(documentSnapshot.getId(), String.valueOf(documentSnapshot.get("count")), String.valueOf(documentSnapshot.get("lastVisited")));
                        totalCount[0] += Integer.parseInt(String.valueOf(documentSnapshot.get("count")));
                    });
                    totalCheckIns.setText(String.valueOf(totalCount[0]));
                    if (totalCount[0] == 0) {
                        checkInHistory.setText("You haven't checked in to any places yet");
                    }
                });

        username.setText(user.getDisplayName());
        email.setText(user.getProviderData().get(1).getEmail());
        dateOfJoining.setText(new SimpleDateFormat("MM/dd/yyyy").format(new Date(user.getMetadata().getCreationTimestamp())));
        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);

    }

    void setPlacesVisitedText(String placeID, String count, String lastVisited) {
        checkInHistory.setVisibility(View.INVISIBLE);
        db.collection("markers").document(placeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                StringBuilder place = new StringBuilder("");
                place.append(String.valueOf(documentSnapshot.get("name"))).append(", visited - ").append(count).append(" time(s)");
                checkInList.add(new CheckInHistory(String.valueOf(documentSnapshot.get("name")), count, lastVisited, placeID, null));
                checkInAdapter.notifyItemInserted(checkInList.size());
            }
        });
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
        } else if (requestCode == UPDATE_PROFILE_REQUEST_CODE) {
            setUserDP(user.getPhotoUrl().toString());
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
        Intent intent = new Intent(getApplicationContext(), EditUserProfileActivity.class);
        startActivityForResult(intent, UPDATE_PROFILE_REQUEST_CODE);
    }
}
