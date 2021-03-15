package com.holzhausen.mediastore.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.holzhausen.mediastore.model.MultimediaItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface MultimediaItemDao {

    @Insert
    Completable insert(MultimediaItem multimediaItem);

    @Update
    Completable update(MultimediaItem multimediaItem);

    @Delete
    Completable delete(MultimediaItem multimediaItem);

    @Query("SELECT * FROM MultimediaItem")
    Flowable<List<MultimediaItem>> getAll();

    @Query("SELECT COUNT(*) FROM MultimediaItem WHERE fileName=:fileName")
    Single<Integer> numberOfItemsWithProvidedFileName(String fileName);

}
