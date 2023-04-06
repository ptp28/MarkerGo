package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class AllLocationPhotosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Integer> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_location_photos);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        imageGridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey("AllImages")) {
            imageList = bundle.getIntegerArrayList("AllImages");
        }
        else {
            imageList = new ArrayList<>();
        }

        recyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);

        recyclerViewImages.setAdapter(recyclerViewAdapter);
        recyclerViewImages.setHasFixedSize(true);
    }
}