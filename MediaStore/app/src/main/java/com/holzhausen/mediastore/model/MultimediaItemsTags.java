package com.holzhausen.mediastore.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class MultimediaItemsTags {

    @Embedded
    private MultimediaItem multimediaItem;

    @Relation(
            parentColumn = "fileName",
            entityColumn = "tagName",
            associateBy = @Junction(MultimediaItemTagCrossRef.class)
    )
    private List<Tag> tags;

    public MultimediaItem getMultimediaItem() {
        return multimediaItem;
    }

    public void setMultimediaItem(MultimediaItem multimediaItem) {
        this.multimediaItem = multimediaItem;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
