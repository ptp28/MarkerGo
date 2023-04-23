package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.markergo.utils.UrlToBitmap;

public class AllLocationPhotosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewImages;
    RecyclerView.LayoutManager imageGridLayoutManager;
    ImageRecyclerViewAdapter recyclerViewAdapter;
    List<Bitmap> imageList;
    List<Uri> imageSources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_location_photos);

        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageGridLayoutManager = new GridLayoutManager(this, 3);
        } else {
            imageGridLayoutManager = new GridLayoutManager(this, 6);
        }
        recyclerViewImages.setLayoutManager(imageGridLayoutManager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("AllImagesSources")) {
            imageSources = (List<Uri>) bundle.getSerializable("AllImagesSources");
        } else {
            imageSources = new ArrayList<>();
        }
        imageList = new ArrayList<>();


        recyclerViewAdapter = new ImageRecyclerViewAdapter(imageList);
        recyclerViewImages.setAdapter(recyclerViewAdapter);
        recyclerViewImages.setHasFixedSize(true);

        downloadImages();
    }

    private void downloadImages() {
        for (Uri uri : imageSources) {
            UrlToBitmap whatever = new UrlToBitmap(uri.toString());
            Thread thread = new Thread(whatever);
            thread.start();
            try {
                thread.join();
                Bitmap image = whatever.getImageBitmap();
                imageList.add(image);
                recyclerViewAdapter.notifyItemInserted(imageList.size());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}