package com.holzhausen.mediastore.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.holzhausen.mediastore.model.MultimediaItemTagCrossRef;

import java.util.List;

import io.reactivex.Completable;

@Dao
public interface MultimediaItemTagCrossRefDao {

    @Insert
    @Transaction
    Completable insert(MultimediaItemTagCrossRef... crossRefs);

    @Query("DELETE FROM MultimediaItemTagCrossRef WHERE fileName=:fileName")
    @Transaction
    Completable delete(String fileName);

}
