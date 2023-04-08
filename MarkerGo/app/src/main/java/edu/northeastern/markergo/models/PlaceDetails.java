package edu.northeastern.markergo.models;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceDetails {
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String description;
    private final Map<String, Integer> visitationStatsByTime;
    private final int visitationsThisWeek;
    private final List<PhotoDetails> photos;

    public PlaceDetails() {
        this.name = null;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.description = null;
        this.visitationStatsByTime = new HashMap<>();
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
    }

    public PlaceDetails(String name, double latitude, double longitude, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.visitationStatsByTime = new HashMap<>();
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
    }

    public PlaceDetails(String name, double latitude, double longitude,
                        String description,
                        Map<String, Integer> visitationStatsByTime,
                        int visitationsThisWeek, List<PhotoDetails> photos) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.visitationStatsByTime = visitationStatsByTime;
        this.visitationsThisWeek = visitationsThisWeek;
        this.photos = photos;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getVisitationStatsByTime() {
        return visitationStatsByTime;
    }

    public int getVisitationsThisWeek() {
        return visitationsThisWeek;
    }

    public List<PhotoDetails> getPhotos() {
        return photos;
    }
}
