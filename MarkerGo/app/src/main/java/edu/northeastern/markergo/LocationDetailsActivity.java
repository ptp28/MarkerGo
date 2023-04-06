package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class LocationDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Integer> imageList;
    private TextView descriptionTextView;
    private TextView seeAllPhotosLinkTextView;
    private Button getDirectionsButton;
    private Button checkInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

        descriptionTextView = (TextView) findViewById(R.id.textViewDescription);
        getDirectionsButton = (Button) findViewById(R.id.buttonGetDirections);
        seeAllPhotosLinkTextView = (TextView) findViewById(R.id.textViewAllPhotosLink);
        checkInButton = (Button) findViewById(R.id.buttonCheckIn);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        imageGridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);

        imageList = new ArrayList<>();
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

        getDirectionsButton.setOnClickListener(getDirectionsListener);
    }

    private void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    View.OnClickListener checkInButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(LocationDetailsActivity.this, "Check In", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener getDirectionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(LocationDetailsActivity.this, "Get Directions", Toast.LENGTH_SHORT).show();
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

    public void testFunction(View view) {
        Toast.makeText(this, "Morning selected", Toast.LENGTH_SHORT).show();
    }
}