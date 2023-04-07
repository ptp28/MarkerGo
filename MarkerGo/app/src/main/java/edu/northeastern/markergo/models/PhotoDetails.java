package edu.northeastern.markergo.models;

public class PhotoDetails {
    private final String url;
    private final String uploadedBy;

    public PhotoDetails(String url, String uplaodedBy) {
        this.url = url;
        this.uploadedBy = uplaodedBy;
    }

    public String getUrl() {
        return url;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

}