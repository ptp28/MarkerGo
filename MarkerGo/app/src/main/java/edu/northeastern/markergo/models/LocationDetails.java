package edu.northeastern.markergo.models;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationDetails {
    private final String name;
    private final Location location;
    private final String description;
    private final Map<String, Integer> visitationStatsByTime;
    private final int visitationsThisWeek;
    private final List<PhotoDetails> photos;

    public LocationDetails() {
        this.name = null;
        this.location = null;
        this.description = null;
        this.visitationStatsByTime = new HashMap<>();
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
    }

    public LocationDetails(String name, Location location, String description) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.visitationStatsByTime = new HashMap<>();
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
    }

    public LocationDetails(String name, Location location,
                           String description,
                           Map<String, Integer> visitationStatsByTime,
                           int visitationsThisWeek, List<PhotoDetails> photos) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.visitationStatsByTime = visitationStatsByTime;
        this.visitationsThisWeek = visitationsThisWeek;
        this.photos = photos;
    }
}
