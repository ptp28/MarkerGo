package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Integer> imageList;
    private TextView descriptionTextView;
    private TextView seeAllPhotosLinkTextView;
    private TextView addPhotoTextView;
    private Button getDirectionsButton;
    private Button checkInButton;

    private Location currentLocation;
    private StorageReference storageRef;
    private StorageReference imagesRef;

    public static final int PICK_IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

        Bundle bundle = getIntent().getExtras();
        storageRef = FirebaseStorage.getInstance().getReference();
        imagesRef = storageRef.child(bundle.get("location") + "/");

        descriptionTextView = (TextView) findViewById(R.id.textViewDescription);
        getDirectionsButton = (Button) findViewById(R.id.buttonGetDirections);
        seeAllPhotosLinkTextView = (TextView) findViewById(R.id.textViewAllPhotosLink);
        addPhotoTextView = (TextView) findViewById(R.id.textViewAddPhoto);
        checkInButton = (Button) findViewById(R.id.buttonCheckIn);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        imageGridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);

        if (bundle != null && bundle.containsKey("currentLocation")) {
            currentLocation = (Location) bundle.get("currentLocation");
        } else {
            imageList = new ArrayList<>();
        }

        imageList = new ArrayList<>();

        populateImageList();

        imageList.add(R.drawable.lake);
        imageList.add(R.drawable.fort);
        imageList.add(R.drawable.bridge);
        imageList.add(R.drawable.fort1);
        imageList.add(R.drawable.monastery);
        imageList.add(R.drawable.fort2);
        imageList.add(R.drawable.palace);
        imageList.add(R.drawable.fort3);
        imageList.add(R.drawable.tent);
        imageList.add(R.drawable.fort4);
        recyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);

        recyclerViewImages.setAdapter(recyclerViewAdapter);
        recyclerViewImages.setHasFixedSize(true);

        checkInButton.setOnClickListener(checkInButtonListener);
        seeAllPhotosLinkTextView.setOnClickListener(openAllPhotosActivityListner);
        addPhotoTextView.setOnClickListener(addPhotosClickListener);

        getDirectionsButton.setOnClickListener(getDirectionsListener);
    }

    private void populateImageList() {
        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference ref : listResult.getItems()) {
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.i("idk", uri.toString());
                            // imageList.add(uri.toString());
                        });
                    }
                    // call recycler view stuff
                });
    }

    private void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    View.OnClickListener checkInButtonListener = new View.OnClickListener() {
        private double latitude = 0;
        private double longitude = 0;

        private float[] distance = new float[10];
//        Location.distanceBetween(this.currentLocation.getLatitude(), currentLocation.getLongitude(), this.latitude, this.longitude, distance);

        @Override
        public void onClick(View v) {
            Toast.makeText(LocationDetailsActivity.this, "Check In", Toast.LENGTH_SHORT).show();
        }
    };

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
            photoRecyclerIntent.putIntegerArrayListExtra("AllImages", (ArrayList<Integer>) imageList);
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
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
                        // imageList.add(uri.toString());
                        // refresh recycler view
                    } else {
                        Log.i("status", "failed to get download url");
                    }
                });
    }
}