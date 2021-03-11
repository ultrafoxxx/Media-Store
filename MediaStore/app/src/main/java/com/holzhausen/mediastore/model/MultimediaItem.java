package com.holzhausen.mediastore.model;

import android.graphics.Bitmap;

import java.util.Date;

public class MultimediaItem {

    private final String fileName;

    private final Date creationDate;

    private final MultimediaType multimediaType;

    private final boolean isLiked;

    private Bitmap preview;

    public MultimediaItem(String fileName, MultimediaType multimediaType, boolean isLiked) {
        this.fileName = fileName;
        this.multimediaType = multimediaType;
        this.creationDate = new Date();
        this.isLiked = isLiked;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public MultimediaType getMultimediaType() {
        return multimediaType;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public void setPreview(Bitmap preview) {
        this.preview = preview;
    }
}
