package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
    private DocumentReference userRef;
    private LatLng point;
    private Dialog dialog;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);
        getSupportActionBar().setTitle("Add new location");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(user.getUid());
        markersRef = db.collection("markers");
        point = (LatLng) getIntent().getExtras().get("location");

        locationText = findViewById(R.id.location);
        descriptionText = findViewById(R.id.desc);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addNewLocation(View view) {
        String location = locationText.getText().toString();
        String description = descriptionText.getText().toString();
        String addedBy = user.getUid();

        if (location.isEmpty() && description.isEmpty()) {
            locationText.setError("Location cannot be empty");
            descriptionText.setError("Description cannot be empty");
        } else if (location.isEmpty()) {
            locationText.setError("Location cannot be empty");
        } else if (description.isEmpty()) {
            descriptionText.setError("Description cannot be empty");
        } else {
            showConfirmationDialog();
        }
    }

    private void showConfirmationDialog() {
        View content = inflater.inflate(R.layout.alert_dialog, null);
        TextView titleTV = (TextView) content.findViewById(R.id.txttite);
        titleTV.setText("Confirm Addition");
        TextView textTV = (TextView) content.findViewById(R.id.txtDesc);
        textTV.setText("500 points will be deducted on addition of a new location. Are you sure you want to proceed?");
        Button checkoutBtn = content.findViewById(R.id.btn_checkout);
        checkoutBtn.setVisibility(View.GONE);

        dialog = new Dialog(this);
        dialog.setContentView(content);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);
        dialog.show();
    }

    public void positiveDialogAction(View view) {
        String location = locationText.getText().toString();
        String description = descriptionText.getText().toString();
        String addedBy = user.getUid();

        Map<String, Object> newLocation = new HashMap<>();
        newLocation.put("name", location);
        newLocation.put("latitude", point.latitude);
        newLocation.put("longitude", point.longitude);
        newLocation.put("description", description);
        Map<String, Long> visitationStatsByTime = getStats();
        newLocation.put("visitationStatsByTime", visitationStatsByTime);
        newLocation.put("visitationsThisWeek", 0);
        newLocation.put("photos", new ArrayList<>());
        newLocation.put("addedBy", user.getUid());

        markersRef.add(newLocation).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            Intent data = new Intent();
            PlaceDetails markerDetails = new PlaceDetails(id, location, point.latitude, point.longitude, description, visitationStatsByTime, addedBy);
            data.putExtra("markerDetails", (Parcelable) markerDetails);
            setResult(RESULT_OK, data);
            userRef.update("points", FieldValue.increment(-500));
            finish();
        });
    }

    public void cancelDialog(View view) {
        dialog.cancel();
    }

    private Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("Morning", 0L);
        stats.put("Afternoon", 0L);
        stats.put("Evening", 0L);
        stats.put("Night", 0L);
        return stats;
    }
}