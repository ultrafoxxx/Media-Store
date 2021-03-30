package com.holzhausen.mediastore.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.holzhausen.mediastore.model.Tag;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Tag... tags);

    @Query("SELECT COUNT(*) FROM Tag WHERE tagName=:tag")
    Single<Integer> howManyTagsExistWithTag(String tag);

}
