package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.WriteResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.markergo.models.PlaceDetails;

public class LocationDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Bitmap> imageList;
    private TextView descriptionTextView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView appbarCoverImage;
    private TextView seeAllPhotosLinkTextView;
    private TextView addPhotoTextView;
    private Button getDirectionsButton;
    private Button checkInButton;

    private Location currentLocation;
    private PlaceDetails markerDetails;
    private StorageReference storageRef;
    private StorageReference imagesRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference usersCollectionRef;
    private CollectionReference markersCollectionRef;
    private Calendar calendar;

    public static final int PICK_IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        db = FirebaseFirestore.getInstance();
        markersCollectionRef = db.collection("markers");
        usersCollectionRef = db.collection("users");

        descriptionTextView = findViewById(R.id.textViewDescription);
        collapsingToolbarLayout = findViewById(R.id.CollapsingToolbarLayout);
        appbarCoverImage = findViewById(R.id.app_bar_image);
        getDirectionsButton = findViewById(R.id.buttonGetDirections);
        seeAllPhotosLinkTextView = findViewById(R.id.textViewAllPhotosLink);
        addPhotoTextView = findViewById(R.id.textViewAddPhoto);
        checkInButton = findViewById(R.id.buttonCheckIn);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        imageGridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);
        recyclerViewImages.setHasFixedSize(true);

        seeAllPhotosLinkTextView.setOnClickListener(openAllPhotosActivityListner);
        addPhotoTextView.setOnClickListener(addPhotosClickListener);

        getDirectionsButton.setOnClickListener(getDirectionsListener);


        storageRef = FirebaseStorage.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("markerDetails")) {
            markerDetails = (PlaceDetails) bundle.get("markerDetails");
            imagesRef = storageRef.child(markerDetails.getName() + "/");
            this.setDescription(markerDetails.getDescription());
            this.setToolbarTitle(markerDetails.getName());
        } else {
            //TODO: What if marker not passed
            markerDetails = new PlaceDetails();
            imagesRef = storageRef.child("dummy-does-not-exist-name/");
        }
        if (bundle.containsKey("currentLocation")) {
            currentLocation = (Location) bundle.get("currentLocation");
        } else {
            //TODO: if currentLocation not passed
            currentLocation = new Location("");
        }

        imageList = new ArrayList<>();
        recyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);
        recyclerViewImages.setAdapter(recyclerViewAdapter);


        populateImageList();
    }

    private void populateImageList() {
        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference ref : listResult.getItems()) {
                        ref.getDownloadUrl().addOnSuccessListener(this::addToImageList);
                    }
                });
    }

    private void addToImageList(Uri uri) {
        Thread addToImageList = new Thread(new UrlToBitmap(uri.toString()));
        addToImageList.start();
    }

    private void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    private void setToolbarTitle(String name) {
        this.collapsingToolbarLayout.setTitle(name);
    }

    private void setToolbarImage(Bitmap image) {
        this.appbarCoverImage.setImageBitmap(image);
    }

    public void checkInToLocation(View view) {
        float[] result = new float[4];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), markerDetails.getLatitude(), markerDetails.getLongitude(), result);
        if (result[0] <= 100) {
            if (user != null) {
                updateVisitationStatsForMarker();
                updateCheckInForUser();
            } else {
                Toast.makeText(getApplicationContext(), "sign in to update stuff on firebase", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "failed. not within 100m", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVisitationStatsForMarker() {
        DocumentReference markerRef = markersCollectionRef.document(markerDetails.getId());
        String field = "visitationStatsByTime." + getSubfield();

        markerRef.update(field, FieldValue.increment(1))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "updated visitationStatsByTime", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.i("status", "failed to update visitationStats"));
    }

    private String getSubfield() {
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return timeOfDay < 12 ? "Morning" : (timeOfDay < 16 ? "Afternoon" : (timeOfDay < 21 ? "Evening" : "Night"));
    }

    private void updateCheckInForUser() {
        DocumentReference userRef = usersCollectionRef.document(user.getUid());
        userRef.update("placesVisited", FieldValue.arrayUnion(markerDetails.getId()))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "checked-in on firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.i("status", "failed to check-in on firebase"));
        userRef.update("points", FieldValue.increment(100));
    }

    View.OnClickListener getDirectionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(LocationDetailsActivity.this, "Get Directions", Toast.LENGTH_SHORT).show();
            String locationURI = ("http://maps.google.com/maps?saddr=Current%20Location&daddr=" + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + "");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(locationURI));
            startActivity(browserIntent);
        }
    };

    View.OnClickListener openAllPhotosActivityListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent photoRecyclerIntent = new Intent(getApplicationContext(), AllLocationPhotosActivity.class);
            startActivity(photoRecyclerIntent);
        }
    };

    View.OnClickListener addPhotosClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent, captureIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST_CODE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                        addToImageList(uri);
                    } else {
                        Log.i("status", "failed to get download url");
                    }
                });
    }

    private class UrlToBitmap implements Runnable {
        URL url;

        UrlToBitmap(String link) {
            try {
                this.url = new URL(link);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                runOnUiThread(() -> {
                    imageList.add(image);
                    recyclerViewAdapter.notifyItemInserted(imageList.size());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}