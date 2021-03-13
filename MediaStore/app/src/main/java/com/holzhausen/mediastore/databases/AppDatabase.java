package com.holzhausen.mediastore.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.holzhausen.mediastore.converters.Converters;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.model.MultimediaItem;

@Database(entities = {MultimediaItem.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract MultimediaItemDao multimediaItemDao();

}
