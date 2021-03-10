package com.holzhausen.mediastore.model;

import java.util.Date;

public class MultimediaItem {

    private final String fileName;

    private final String fileLocation;

    private final Date creationDate;

    private final MultimediaType multimediaType;

    public MultimediaItem(String fileName, String fileLocation, MultimediaType multimediaType) {
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        this.multimediaType = multimediaType;
        this.creationDate = new Date();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public MultimediaType getMultimediaType() {
        return multimediaType;
    }
}
