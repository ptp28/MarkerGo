package edu.northeastern.markergo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import edu.northeastern.markergo.models.PlaceDetails;
import edu.northeastern.markergo.models.VisitationDetails;
import edu.northeastern.markergo.utils.UrlToBitmap;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class LocationDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Bitmap> imageList;

    List<Uri> imageSources;
    private TextView descriptionTextView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView appbarCoverImage;
    private TextView seeAllPhotosLinkTextView;
    private TextView addPhotoTextView;
    private TextView lastVisitTextView;
    private Button getDirectionsButton;
    private Button checkInButton;
    private Location currentLocation;
    private PlaceDetails markerDetails;
    private VisitationDetails visitationDetails;
    private StorageReference storageRef;
    private StorageReference imagesRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference usersCollectionRef;
    private CollectionReference markersCollectionRef;
    private Calendar calendar;
    private boolean checkedIn = false;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm");
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private KonfettiView konfettiView;
    private Party party;
    private long lastVisited;
    private Dialog dialog;
    private LayoutInflater inflater;

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

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart);
        Shape.DrawableShape drawableShape = new Shape.DrawableShape(drawable, true);

        konfettiView = findViewById(R.id.konfettiView);
        EmitterConfig emitterConfig = new Emitter(2L, TimeUnit.SECONDS).perSecond(400);
        party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeed(50f)
                .timeToLive(4000L)
                .shapes(new Shape.Rectangle(0.2f), drawableShape)
                .sizes(new Size(12, 5f, 0.2f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        descriptionTextView = findViewById(R.id.textViewDescription);
        collapsingToolbarLayout = findViewById(R.id.CollapsingToolbarLayout);
        appbarCoverImage = findViewById(R.id.app_bar_image);
        getDirectionsButton = findViewById(R.id.buttonGetDirections);
        seeAllPhotosLinkTextView = findViewById(R.id.textViewAllPhotosLink);
        addPhotoTextView = findViewById(R.id.textViewAddPhoto);
        checkInButton = findViewById(R.id.buttonCheckIn);
        lastVisitTextView = findViewById(R.id.textViewLastVisitLabel);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageGridLayoutManager = new GridLayoutManager(this, 3);
        } else {
            imageGridLayoutManager = new GridLayoutManager(this, 6);
        }
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);
        recyclerViewImages.setHasFixedSize(true);

        seeAllPhotosLinkTextView.setOnClickListener(openAllPhotosActivityListner);
        addPhotoTextView.setOnClickListener(addPhotosClickListener);

        getDirectionsButton.setOnClickListener(getDirectionsListener);


        storageRef = FirebaseStorage.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("markerDetails")) {
            markerDetails = (PlaceDetails) bundle.get("markerDetails");

            if (bundle.containsKey("visitationDetails")) {
                visitationDetails = (VisitationDetails) bundle.get("visitationDetails");
                this.lastVisited = visitationDetails.getLastVisited();
                setLastVisitedText(this.lastVisited);
            } else {
                this.lastVisited = 0;
                String text = "You have never visited this place.";
                lastVisitTextView.setText(text);
            }

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
        imageSources = new ArrayList<>();
        recyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);
        recyclerViewImages.setAdapter(recyclerViewAdapter);


        populateImageList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.profile_item:
                startActivity(new Intent(LocationDetailsActivity.this, UserProfileActivity.class));
                break;
            case R.id.logout_item:
                mAuth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LocationDetailsActivity.this, MainActivity.class));
                break;
            case R.id.favourites_item:
                //favourites
                break;
            case R.id.home_item:
                startActivity(new Intent(LocationDetailsActivity.this, landingPage.class));
                break;
            default:
                break;
        }
        menuItem.setChecked(true);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("checkedIn", checkedIn);
        intent.putExtra("markerDetails", markerDetails);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void populateImageList() {
        imagesRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference ref : listResult.getItems()) {
                        ref.getDownloadUrl().addOnSuccessListener(this::addToImageList);
                    }
                });
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
                .addOnFailureListener(e -> Log.i("status", "failed to update visitationStats"));
    }

    private String getSubfield() {
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return timeOfDay < 12 ? "Morning" : (timeOfDay < 16 ? "Afternoon" : (timeOfDay < 21 ? "Evening" : "Night"));
    }

    private void updateCheckInForUser() {
        DocumentReference userRef = usersCollectionRef.document(user.getUid());
        DocumentReference placeRef = userRef.collection("placesVisited").document(markerDetails.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("count", FieldValue.increment(1));
        long prevLastVisited = this.lastVisited;
        this.lastVisited = new Date().getTime();
        data.put("lastVisited", lastVisited);

        placeRef.set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    this.checkedIn = true;
                    setLastVisitedText(lastVisited);

                    LocalDateTime oldDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(prevLastVisited), TimeZone.getDefault().toZoneId());
                    LocalDateTime newDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastVisited), TimeZone.getDefault().toZoneId());

                    if (newDate.getYear() != oldDate.getYear() ||
                            newDate.getMonth() != oldDate.getMonth() ||
                            newDate.getDayOfMonth() != oldDate.getDayOfMonth()) {
                        konfettiView.start(party);
                        showPointsEarnedDialog();
                        userRef.update("points", FieldValue.increment(100));
                        // display box of points gained and total points
                    } else {
                        showNoPointsEarnedDialog();
                    }
                })
                .addOnFailureListener(e -> Log.i("status", "failed to check-in on firebase"));
    }

    private void showPointsEarnedDialog() {
        showDialog("You received 100 points for checking in to this place!");
    }

    private void showNoPointsEarnedDialog() {
        showDialog("You did not receive points since your latest check-in for this place is today.");
    }

    void showDialog(String text) {
        View content = inflater.inflate(R.layout.points_dialog, null);
        TextView tv = (TextView) content.findViewById(R.id.txtDesc);
        tv.setText(text);

        dialog = new Dialog(this);
        dialog.setContentView(content);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.show();
    }

    public void cancelDialog(View view) {
        dialog.cancel();
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
            photoRecyclerIntent.putExtra("AllImagesSources", (Serializable) imageSources);
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

    private void addToImageList(Uri uri) {
        UrlToBitmap whatever = new UrlToBitmap(uri.toString());
        Thread thread = new Thread(whatever);
        thread.start();
        try {
            thread.join();
            Bitmap image = whatever.getImageBitmap();
            imageSources.add(uri);
            imageList.add(image);
            recyclerViewAdapter.notifyItemInserted(imageList.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setLastVisitedText(long lastVisited) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastVisited), TimeZone.getDefault().toZoneId());
        String text = "You last visited this place on " + formatter.format(dateTime);
        lastVisitTextView.setText(text);
    }
}