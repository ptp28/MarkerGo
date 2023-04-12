package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.markergo.models.PlaceDetails;

public class newLocation extends AppCompatActivity {
    private EditText locationText;
    private EditText descriptionText;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference markersRef;
    private LatLng point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);
        getSupportActionBar().setTitle("Add new location");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        markersRef = db.collection("markers");
        point = (LatLng) getIntent().getExtras().get("location");

        locationText = findViewById(R.id.location);
        descriptionText = findViewById(R.id.desc);
    }

    public void addNewLocation(View view) {
        String location = locationText.getText().toString();
        String description = descriptionText.getText().toString();

        if (location.isEmpty() && description.isEmpty()) {
            locationText.setError("Location cannot be empty");
            descriptionText.setError("Description cannot be empty");
        } else if (location.isEmpty()) {
            locationText.setError("Location cannot be empty");
        } else if (description.isEmpty()) {
            descriptionText.setError("Description cannot be empty");
        } else {
            Map<String, Object> newLocation = new HashMap<>();
            newLocation.put("name", location);
            newLocation.put("latitude", point.latitude);
            newLocation.put("longitude", point.longitude);
            newLocation.put("description", description);
            newLocation.put("visitationStatsByTime", getStats());
            newLocation.put("visitationsThisWeek", 0);
            newLocation.put("photos", new ArrayList<>());
            newLocation.put("addedBy", user.getUid());

            markersRef.add(newLocation).addOnSuccessListener(documentReference -> {
                String id = documentReference.getId();
                Intent data = new Intent();
                PlaceDetails markerDetails = new PlaceDetails(id, location, point.latitude, point.longitude, description);
                data.putExtra("markerDetails", (Parcelable) markerDetails);
                setResult(RESULT_OK, data);
                finish();
            });
        }
    }

    private Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Morning", 0);
        stats.put("Afternoon", 0);
        stats.put("Evening", 0);
        stats.put("Night", 0);
        return stats;
    }
}