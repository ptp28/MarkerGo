package edu.northeastern.markergo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class EditUserProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private StorageReference storageRef;
    private StorageReference imagesRef;

    private EditText usernameInput;
    private ImageView userDP;

    private LinearLayout oldPasswordLayout;
    private LinearLayout newPasswordLayout;
    private EditText oldPasswordInput;
    private EditText newPasswordInput;

    private static int PICK_IMAGE_REQUEST_CODE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
        imagesRef = storageRef.child("customUserPhotos/");

        usernameInput = findViewById(R.id.usernameInput);
        usernameInput.setText(user.getDisplayName());
        userDP = findViewById(R.id.userDP);
        String photoUrl = String.valueOf(user.getPhotoUrl());
        setUserDP(photoUrl);

        usernameInput = findViewById(R.id.usernameInput);
        usernameInput.setText(user.getDisplayName());

        oldPasswordLayout = findViewById(R.id.oldPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        oldPasswordInput = findViewById(R.id.oldPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);

        String providerType = user.getProviderData().get(1).getProviderId();
        if(providerType.equals("google.com") || providerType.equals("facebook.com")) {
            oldPasswordLayout.setVisibility(View.INVISIBLE);
            newPasswordLayout.setVisibility(View.INVISIBLE);
        }

    }

    public void changePassword(View view) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPasswordInput.getText().toString());

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPasswordInput.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                        Log.d("PASS CHANGE SUCCESS", "Password updated");
                                    } else {
                                        Toast.makeText(getApplicationContext(), "...???IDK", Toast.LENGTH_SHORT).show();
                                        Log.d("PASS CHANGE ERROR", "Error password not updated");
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Password does not match our records", Toast.LENGTH_SHORT).show();
                            Log.d("PASS CHANGE FAIL", "Error auth failed");
                        }
                    }
                });
    }

    public void updateUserProfile(View view) {
        String uid = user.getUid();
        System.out.println("UID -> " + uid);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(usernameInput.getText().toString())
                .build();
        assert user != null;
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").document(uid)
                        .update(
                                "name", usernameInput.getText().toString()
                        ).addOnSuccessListener(task2 -> {

                            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            Toast.makeText(getApplicationContext(),"User update successfully", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(getApplicationContext(), "Error in updating user profile", Toast.LENGTH_SHORT).show();
            }
        });

//        String uid = user.getUid();
//        System.out.println("UID -> " + uid);
//
//        db.collection("users").document(uid)
//                .update(
//                        "name", usernameInput.getText().toString(),
//                        "email", emailInput.getText().toString()
//                ).addOnSuccessListener(task -> {
//                    user.updateEmail(emailInput.getText().toString());
//                    user.updateProfile(new UserProfileChangeRequest.Builder().
//                            setDisplayName(usernameInput.getText().toString()).build());
//                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//
//                    Toast.makeText(getApplicationContext(),"User update successfully", Toast.LENGTH_SHORT).show();
//                });
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


    public void setUserDP(String photoUrl) {
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
}
