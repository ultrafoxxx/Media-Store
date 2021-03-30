package com.holzhausen.mediastore.model;

import androidx.room.Entity;
import androidx.room.Fts4;

@Entity
@Fts4(contentEntity = MultimediaItemTagCrossRef.class)
public class MultimediaItemTagCrossRefFTS {

    private String fileName;

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
