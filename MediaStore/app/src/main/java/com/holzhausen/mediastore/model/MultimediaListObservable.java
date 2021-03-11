package com.holzhausen.mediastore.model;

import java.util.List;
import java.util.Observable;

public class MultimediaListObservable extends Observable {

    private final List<MultimediaItem> multimediaItems;

    public MultimediaListObservable(List<MultimediaItem> multimediaItems) {
        this.multimediaItems = multimediaItems;
    }

    public void addMultimediaItem(final MultimediaItem multimediaItem){
        multimediaItems.add(multimediaItem);
        setChanged();
        notifyObservers(multimediaItems);
    }
}
