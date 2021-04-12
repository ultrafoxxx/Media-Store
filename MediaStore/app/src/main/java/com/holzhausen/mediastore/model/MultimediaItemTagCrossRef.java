package com.holzhausen.mediastore.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Fts4;

@Entity(primaryKeys = {"fileName", "tagName"})
public class MultimediaItemTagCrossRef {

    public MultimediaItemTagCrossRef(String fileName, String tagName) {
        this.fileName = fileName;
        this.tagName = tagName;
    }

    public MultimediaItemTagCrossRef() {
    }

    @NonNull
    private String fileName;

    @NonNull
    private String tagName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
