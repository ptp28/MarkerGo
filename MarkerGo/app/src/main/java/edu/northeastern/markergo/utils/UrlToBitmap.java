package edu.northeastern.markergo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

public class UrlToBitmap implements Runnable {
    URL url;
    Bitmap image;

    public UrlToBitmap(String link) {
        try {
            this.url = new URL(link);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Bitmap getImageBitmap() {
        return image;
    }
}
