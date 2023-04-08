package edu.northeastern.markergo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class landingPage extends AppCompatActivity implements OnMapReadyCallback {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public double latitude;
    public double longitude;
    LocationRequest locationRequest;
    Marker currentLocationMarker;

    private Location currentLocation;
    private Location markerLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int FINE_LOCATION_REQUEST_CODE = 10;
    private static final String TAG = "landingPage.java";
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private FirebaseFirestore fireStoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        fireStoreDB = FirebaseFirestore.getInstance();
        populateMarkers();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currentLocation = location;
                UpdateCurrentLocation();
            }
        };
        askRequiredLocationPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawerLayout = findViewById(R.id.my_drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The app will need to access location to display latitude and longitude. " +
                        "Please allow the permission to use location services on the next window.")
                .setPositiveButton("Ok", (dialog, which) ->
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE))
                .show();

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
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.user_marker, getTheme());
        Bitmap userMarker = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), 100, 156, false);

        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .title("{{User Name}}")
                .icon(BitmapDescriptorFactory.fromBitmap(userMarker));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        currentLocationMarker = googleMap.addMarker(markerOptions);
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                openLocationDetails(marker);
            }
        });
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void openLocationDetails(Marker marker) {
        LatLng latlng = marker.getPosition();
        markerLocation = new Location("");
        markerLocation.setLatitude(latlng.latitude);
        markerLocation.setLongitude(latlng.longitude);

        Intent locationDetailsActivityIntent = new Intent(getApplicationContext(), LocationDetailsActivity.class);
        locationDetailsActivityIntent.putExtra("markerLocation", markerLocation);
        locationDetailsActivityIntent.putExtra("currentLocation", currentLocation);
        startActivity(locationDetailsActivityIntent);
    }

    public void populateMarkers() {
        CollectionReference collectionReference = fireStoreDB.collection("markers");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                    if (!documentSnapshots.isEmpty()) {
                        documentSnapshots.forEach(new Consumer<DocumentSnapshot>() {
                            @Override
                            public void accept(DocumentSnapshot documentSnapshot) {
                                Log.d(TAG, String.valueOf(documentSnapshot.get("description")));


                                LatLng latLng = new LatLng((Double) documentSnapshot.get("latitude"), (Double) documentSnapshot.get("longitude"));
                                MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                                        .title((String) documentSnapshot.get("name"));
                                googleMap.addMarker(markerOptions);
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
}