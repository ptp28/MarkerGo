package edu.northeastern.markergo;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, landingPage.class);
            startActivity(intent);
            finish();
            System.exit(0);
        }
        else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            System.exit(0);
        }

//        Button mapsButton = findViewById(R.id.buttonMapsActivity);
//        if (mapsButton != null) {
//            mapsButton.setOnClickListener((it -> {
//                Intent intent = new Intent(MainActivity.this, landingPage.class);
//                startActivity(intent);
//            }));
//        }

//        addMarkers();
    }

    public void openLocationDetailsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), LocationDetailsActivity.class);
        intent.putExtra("location", "placeholder");

        // placeholder
        Location cloc = new Location("");
        cloc.setLatitude(42.3134789);
        cloc.setLongitude(-71.109201);
        intent.putExtra("currentLocation", cloc);

        //placeholder
        Location mloc = new Location("");
        mloc.setLatitude(42.3134789);
        mloc.setLongitude(-71.109201);
        intent.putExtra("markerLocation", mloc);

        startActivity(intent);
    }

    public void openLoginActivity(View view) {
//        if (mAuth.getCurrentUser() != null) {
//            Toast.makeText(
//                            getApplicationContext(),
//                            "already logged in, signing out",
//                            Toast.LENGTH_SHORT)
//                    .show();
//            mAuth.signOut();
//        }
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    public void openUserProfileActivity(View view) {
        startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
    }

//    private void addMarkers() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        InputStream is = getResources().openRawResource(R.raw.updated_places);
//        BufferedReader reader = new BufferedReader(
//                new InputStreamReader(is, StandardCharsets.UTF_8));
//        String line = "";
//
//        try {
//            reader.readLine();
//            int cnt = 0;
//            while ((line = reader.readLine()) != null) {
//                cnt++;
//                // Split the line into different tokens (using the comma as a separator).
//                String[] entry = line.split(",");
//
//                Map<String, Object> data = new HashMap<>();
//                data.put("name", entry[0]);
//                data.put("latitude", Double.parseDouble(entry[1]));
//                data.put("longitude", Double.parseDouble(entry[2]));
//                data.put("description", entry[3]);
//
//                Map<String, Integer> stats = new HashMap<>();
//                stats.put("Morning", 0);
//                stats.put("Afternoon", 0);
//                stats.put("Evening", 0);
//                stats.put("Night", 0);
//                data.put("visitationStatsByTime", stats);
//
//                data.put("visitationsThisWeek", 0);
//                data.put("photos", new ArrayList<>());
//                data.put("addedBy", "XVYEveHBYscKtrAc66xTWDTYWH22");
//
//                int finalCnt = cnt;
//                db.collection("markers")
//                        .add(data)
//                        .addOnCompleteListener(task -> Log.i("status", "done " + finalCnt));
//            }
//        } catch (IOException e1) {
//            Log.e("MainActivity", "Error" + line, e1);
//            e1.printStackTrace();
//        }
//    }
}