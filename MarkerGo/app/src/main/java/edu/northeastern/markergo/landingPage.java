package edu.northeastern.markergo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import edu.northeastern.markergo.models.PlaceDetails;
import edu.northeastern.markergo.models.VisitationDetails;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class landingPage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    // maker colour change
    private LayoutInflater inflater;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentReference userRef;
    //
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    public double latitude;
    public double longitude;
    LocationRequest locationRequest;
    Marker currentLocationMarker;
    private Location currentLocation;

    private Snackbar locationSnackbar;
    private Location markerLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int FINE_LOCATION_REQUEST_CODE = 10;
    private static final int ADD_MARKER_REQUEST_CODE = 100;
    private static final int VISIT_MARKER_REQUEST_CODE = 200;
    private static final String TAG = "landingPage.java";
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FirebaseFirestore fireStoreDB;
    private List<PlaceDetails> markerDetailsList;
    private Marker currMarker;
    private CollectionReference markersRef;
    private LatLng pointToBeAdded;
    private Dialog addLocationDialog;
    private Dialog successDialog;
    private Dialog cantAddLocationDialog;
    private KonfettiView konfettiView;
    private Party party;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_heart);
        Shape.DrawableShape drawableShape = new Shape.DrawableShape(drawable, true);

        locationSnackbar = Snackbar.make((View)findViewById(R.id.mapFrame), "Need Location Permissions to access all functionality.", Snackbar.LENGTH_INDEFINITE)
                .setAction("FIX", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        askToTurnOnGPSInSettings();
                    }
                });
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

        fireStoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        userRef = fireStoreDB.collection("users").document(user.getUid());
        markersRef = fireStoreDB.collection("markers");
        markerDetailsList = new ArrayList<>();
        populateMarkers();

        setUpLocationManagerWithPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    private void setUpLocationManagerWithPermissions() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currentLocation = location;
                UpdateCurrentLocation();
            }
        };
        askRequiredLocationPermissions();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setMessage("Are you sure you want to exit?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        landingPage.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = build.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        int height;
        int width;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.72);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.25);
        } else {
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.6);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.35);
        }
        alertDialog.getWindow().setLayout(width, height);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_item:
                startActivity(new Intent(landingPage.this, UserProfileActivity.class));
                return true;
            case R.id.logout_item:
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(landingPage.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void askRequiredLocationPermissions() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 2, locationListener);
            } else {
                requestFineLocationPermission();
            }
        } else {
            askToTurnOnGPSInSettings();
        }
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
    }

    private void askToTurnOnGPSInSettings() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Location Services").setMessage("GPS and location services seem to be disabled. Would you like to enable them now?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = this.googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.google_maps_style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askRequiredLocationPermissions();
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
    }

    private void UpdateCurrentLocation() {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.user_marker, getTheme());
        Bitmap userMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), 100, 156, false);

        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .title(user.getDisplayName())
                .icon(BitmapDescriptorFactory.fromBitmap(userMarker));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        currentLocationMarker = googleMap.addMarker(markerOptions);
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if (marker.equals(currentLocationMarker)) {
                    openProfileDetailsActivity(marker);
                } else {
                    openLocationDetailsActivity(marker);
                }
            }
        });
        googleMap.setOnMapLongClickListener(this);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                    locationSnackbar.dismiss();
                    setUpLocationManagerWithPermissions();
                } else {
                    locationSnackbar.show();
                }
                break;
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        pointToBeAdded = point;
        markersRef
                .whereEqualTo("latitude", point.latitude)
                .whereEqualTo("longitude", point.longitude)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.getDocuments().size() != 0) {
                        return;
                    }

                    showAddLocationDialog();
                });
    }

    private void showAddLocationDialog() {
        View content = inflater.inflate(R.layout.alert_dialog, null);
        TextView titleTV = content.findViewById(R.id.txttite);
        titleTV.setText("Add new location?");
        TextView textTV = content.findViewById(R.id.txtDesc);
        textTV.setVisibility(View.INVISIBLE);
        Button checkoutBtn = content.findViewById(R.id.btn_checkout);
        checkoutBtn.setVisibility(View.GONE);

        addLocationDialog = new Dialog(this);
        addLocationDialog.setContentView(content);
        addLocationDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        addLocationDialog.show();
    }

    public void positiveDialogAction(View view) {
        addLocationDialog.dismiss();
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> data = documentSnapshot.getData();
            if (data != null && data.containsKey("points") && (long) data.get("points") >= 500) {
                Intent intent = new Intent(landingPage.this, newLocation.class);
                intent.putExtra("location", pointToBeAdded);
                startActivityForResult(intent, ADD_MARKER_REQUEST_CODE);
            } else {
                showCantAddLocationDialog();
            }
        });
    }

    private void showCantAddLocationDialog() {
        View content = inflater.inflate(R.layout.alert_dialog, null);
        ImageView imageIcon = content.findViewById(R.id.img_icon);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.alert_triangle);
        imageIcon.setImageBitmap(icon);
        TextView titleTV = content.findViewById(R.id.txttite);
        titleTV.setText("Cannot Add!");
        TextView textTV = content.findViewById(R.id.txtDesc);
        textTV.setText("You need at-least 500 points to add a new location. Check-in to some places to enable this feature.");
        LinearLayout buttonsLayout = content.findViewById(R.id.buttonsLayout);
        buttonsLayout.setVisibility(View.GONE);

        cantAddLocationDialog = new Dialog(this);
        cantAddLocationDialog.setContentView(content);
        cantAddLocationDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        cantAddLocationDialog.show();
    }

    public void cancelDialog(View view) {
        if (addLocationDialog != null) {
            addLocationDialog.cancel();
        }

        if (successDialog != null) {
            successDialog.cancel();
        }

        if(cantAddLocationDialog != null) {
            cantAddLocationDialog.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_MARKER_REQUEST_CODE & resultCode == RESULT_OK && data != null) {
            PlaceDetails markerDetails = (PlaceDetails) data.getExtras().get("markerDetails");
            System.out.println("id = " + markerDetails.getId());
            setMarkerOnMap(markerDetails);
            showSuccessDialog(markerDetails);
        } else if (requestCode == VISIT_MARKER_REQUEST_CODE & resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if ((Boolean) extras.get("checkedIn")) {
                LatLng position = this.currMarker.getPosition();
                this.currMarker.remove();
                this.currMarker = null;
                PlaceDetails markerDetails = (PlaceDetails) extras.get("markerDetails");
                addGreenMarker(markerDetails, position);
            }
        }
    }

    private void showSuccessDialog(PlaceDetails markerDetails) {
        konfettiView.start(party);
        View content = inflater.inflate(R.layout.alert_dialog, null);
        TextView titleTV = content.findViewById(R.id.txttite);
        titleTV.setText("Location Added!");
        Button noBtn = content.findViewById(R.id.btn_no);
        Button yesBtn = content.findViewById(R.id.btn_yes);
        noBtn.setVisibility(View.GONE);
        yesBtn.setVisibility(View.GONE);
        Button checkoutBtn = content.findViewById(R.id.btn_checkout);
        checkoutBtn.setOnClickListener(v -> {
            successDialog.cancel();
            openLocationDetailsActivity(markerDetails);
        });

        successDialog = new Dialog(this);
        successDialog.setContentView(content);
        successDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        successDialog.show();
    }

    private void addGreenMarker(PlaceDetails markerDetails, LatLng position) {
        final MarkerOptions[] markerOptions = new MarkerOptions[1];
        markerOptions[0] = new MarkerOptions().position(position)
                .title(markerDetails.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        Marker marker = googleMap.addMarker(markerOptions[0]);
        marker.setTag(markerDetails);
    }

    private void addDefaultMarker(PlaceDetails markerDetails, LatLng position) {
        final MarkerOptions[] markerOptions = new MarkerOptions[1];
        markerOptions[0] = new MarkerOptions().position(position)
                .title(markerDetails.getName());
        Marker marker = googleMap.addMarker(markerOptions[0]);
        marker.setTag(markerDetails);
    }

    public void openProfileDetailsActivity(Marker marker) {
        Intent profileDetailsIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
        startActivity(profileDetailsIntent);
    }

    public void openLocationDetailsActivity(Marker marker) {
        this.currMarker = marker;
        PlaceDetails markerDetails = (PlaceDetails) marker.getTag();
        assert markerDetails != null;
        System.out.println(markerDetails.getAddedBy());
        openLocationDetailsActivity(markerDetails);
    }

    private void openLocationDetailsActivity(PlaceDetails markerDetails) {
        userRef
                .collection("placesVisited")
                .document(markerDetails.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Intent locationDetailsActivityIntent = new Intent(getApplicationContext(), LocationDetailsActivity.class);
                    locationDetailsActivityIntent.putExtra("currentLocation", currentLocation);
                    locationDetailsActivityIntent.putExtra("markerDetails", markerDetails);

                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        System.out.println(data);
                        assert data != null;
                        int count = Integer.parseInt(Objects.requireNonNull(data.get("count")).toString());
                        long lastVisited = Long.parseLong(Objects.requireNonNull(data.get("lastVisited")).toString());
                        VisitationDetails visitationDetails = new VisitationDetails(count, lastVisited);
                        locationDetailsActivityIntent.putExtra("visitationDetails", visitationDetails);
                        System.out.println("added");
                    }
                    startActivityForResult(locationDetailsActivityIntent, VISIT_MARKER_REQUEST_CODE);
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_SHORT).show());
    }

    public void populateMarkers() {
        markersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (!documentSnapshots.isEmpty()) {
                        documentSnapshots.forEach(new Consumer<DocumentSnapshot>() {
                            @Override
                            public void accept(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, String.valueOf(documentSnapshot.get("name")));
                                Map<String, Long> visitationStatsByTime = (Map<String, Long>) documentSnapshot.get("visitationStatsByTime");
                                PlaceDetails markerDetails = new PlaceDetails(
                                        documentSnapshot.getId(),
                                        String.valueOf(documentSnapshot.get("name")),
                                        (Double) documentSnapshot.get("latitude"),
                                        (Double) documentSnapshot.get("longitude"),
                                        (String) documentSnapshot.get("description"),
                                        visitationStatsByTime,
                                        (String) documentSnapshot.get("addedBy")
                                );
                                markerDetailsList.add(markerDetails);
                                setMarkerOnMap(markerDetails);
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setMarkerOnMap(PlaceDetails markerDetails) {
        LatLng latLng = new LatLng(markerDetails.getLatitude(), markerDetails.getLongitude());
        if (user != null) {
            userRef.collection("placesVisited")
                    .document(markerDetails.getId())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            addGreenMarker(markerDetails, latLng);
                        } else {
                            addDefaultMarker(markerDetails, latLng);
                        }
                    });
        } else {
            addDefaultMarker(markerDetails, latLng);
        }
    }
}