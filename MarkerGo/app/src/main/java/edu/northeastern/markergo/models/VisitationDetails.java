package edu.northeastern.markergo.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class VisitationDetails implements Parcelable {
    private final int count;
    private final long lastVisited;

    public VisitationDetails(int count, long lastVisited) {
        this.count = count;
        this.lastVisited = lastVisited;
    }

    protected VisitationDetails(Parcel in) {
        count = in.readInt();
        lastVisited = in.readLong();
    }

    public static final Creator<VisitationDetails> CREATOR = new Creator<VisitationDetails>() {
        @Override
        public VisitationDetails createFromParcel(Parcel in) {
            return new VisitationDetails(in);
        }

        @Override
        public VisitationDetails[] newArray(int size) {
            return new VisitationDetails[size];
        }
    };

    public int getCount() {
        return count;
    }

    public long getLastVisited() {
        return lastVisited;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(count);
        dest.writeLong(lastVisited);
    }
}
