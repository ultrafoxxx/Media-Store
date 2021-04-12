package com.holzhausen.mediastore.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Tag {

    @PrimaryKey
    @NonNull
    private String tagName;

    public Tag() {}

    public Tag(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return tagName.equals(tag.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);
    }
}
