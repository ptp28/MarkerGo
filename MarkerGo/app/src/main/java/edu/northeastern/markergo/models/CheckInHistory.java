package edu.northeastern.markergo.models;

import android.graphics.Bitmap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class CheckInHistory {

    private final String name;
    private final String count;
    private final String lastVisited;
    private final String locationID;

    public Bitmap getLocationImageBitmap() {
        return locationImage;
    }

    private final Bitmap locationImage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm");


    public CheckInHistory(String name, String count, String lastVisited, String locationID, Bitmap locationImage) {
        this.name = name;
        this.count = count;
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(lastVisited)), TimeZone.getDefault().toZoneId());
        this.lastVisited = formatter.format(dateTime);
        this.locationID = locationID;
        this.locationImage = locationImage;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

    public String getLocationID() {
        return locationID;
    }

    public String getLastVisited() {
        return lastVisited;
    }
}
