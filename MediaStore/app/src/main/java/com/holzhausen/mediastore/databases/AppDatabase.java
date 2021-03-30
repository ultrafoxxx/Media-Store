package com.holzhausen.mediastore.databases;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.holzhausen.mediastore.converters.Converters;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.daos.MultimediaItemTagCrossRefDao;
import com.holzhausen.mediastore.daos.TagDao;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaItemTagCrossRef;
import com.holzhausen.mediastore.model.MultimediaItemTagCrossRefFTS;
import com.holzhausen.mediastore.model.Tag;

@Database(entities = {
        MultimediaItem.class,
        Tag.class,
        MultimediaItemTagCrossRef.class,
        MultimediaItemTagCrossRefFTS.class
}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract MultimediaItemDao multimediaItemDao();
    public abstract TagDao tagDao();
    public abstract MultimediaItemTagCrossRefDao multimediaItemTagCrossRefDao();

}
