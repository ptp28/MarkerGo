package edu.northeastern.markergo.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceDetails implements Parcelable {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String description;
    private final Map<String, Long> visitationStatsByTime;
    private final int visitationsThisWeek;
    private final List<PhotoDetails> photos;
    private final String addedBy;

    public PlaceDetails() {
        this.id = null;
        this.name = null;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.description = null;
        this.visitationStatsByTime = new HashMap<>();
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
        this.addedBy = null;
    }

    protected PlaceDetails(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.photos = (ArrayList<PhotoDetails>) in.readSerializable();
        this.visitationStatsByTime = (HashMap<String, Long>) in.readSerializable();
        this.visitationsThisWeek = in.readInt();
        this.addedBy = in.readString();
    }

    public PlaceDetails(String id, String name, double latitude, double longitude, String description, Map<String, Long> visitationStatsByTime, String addedBy) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.visitationStatsByTime = visitationStatsByTime;
        this.visitationsThisWeek = 0;
        this.photos = new ArrayList<>();
        this.addedBy = addedBy;
    }

    public static final Creator<PlaceDetails> CREATOR = new Creator<PlaceDetails>() {
        @Override
        public PlaceDetails createFromParcel(Parcel in) {
            return new PlaceDetails(in);
        }

        @Override
        public PlaceDetails[] newArray(int size) {
            return new PlaceDetails[size];
        }
    };

    public PlaceDetails(String id, String name, double latitude, double longitude,
                        String description,
                        Map<String, Long> visitationStatsByTime,
                        int visitationsThisWeek, List<PhotoDetails> photos, String addedBy) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.visitationStatsByTime = visitationStatsByTime;
        this.visitationsThisWeek = visitationsThisWeek;
        this.photos = photos;
        this.addedBy = addedBy;
    }

    public String getId() {
        return id;
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

    public Map<String, Long> getVisitationStatsByTime() {
        return visitationStatsByTime;
    }

    public int getVisitationsThisWeek() {
        return visitationsThisWeek;
    }

    public List<PhotoDetails> getPhotos() {
        return photos;
    }

    public String getAddedBy() {
        return addedBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeSerializable((Serializable) this.photos);
        dest.writeSerializable((Serializable) this.visitationStatsByTime);
        dest.writeInt(this.visitationsThisWeek);
        dest.writeString(this.addedBy);
    }
}
