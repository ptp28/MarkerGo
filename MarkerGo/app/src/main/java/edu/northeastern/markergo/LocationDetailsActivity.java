package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);

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
    }
}