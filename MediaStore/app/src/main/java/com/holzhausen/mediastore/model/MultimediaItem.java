package com.holzhausen.mediastore.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class MultimediaItem {

    @PrimaryKey
    @NonNull
    private final String fileName;

    private final String filePath;

    private Date creationDate;

    private MultimediaType multimediaType;

    private boolean isLiked;

    public MultimediaItem(@NonNull String fileName, String filePath, MultimediaType multimediaType,
                          boolean isLiked) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.multimediaType = multimediaType;
        this.creationDate = new Date();
        this.isLiked = isLiked;
    }

    @NonNull
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

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setMultimediaType(MultimediaType multimediaType) {
        this.multimediaType = multimediaType;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getFilePath() {
        return filePath;
    }
}
