package com.holzhausen.mediastore.application;

import android.app.Application;

import androidx.room.Room;

import com.holzhausen.mediastore.databases.AppDatabase;

public class MediaStoreApp extends Application {

    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "media-store-database").build();
    }

    public AppDatabase getDatabase(){
        return database;
    }
}
