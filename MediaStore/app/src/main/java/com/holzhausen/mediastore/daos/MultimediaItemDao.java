package com.holzhausen.mediastore.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaItemsTags;

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

    @Transaction
    @Query("SELECT * FROM MultimediaItem")
    Flowable<List<MultimediaItemsTags>> getAll();

    @Transaction
    @Query("SELECT * FROM MultimediaItem ORDER BY fileName")
    Flowable<List<MultimediaItemsTags>> getAllItemsSortedByTitleAsc();

    @Transaction
    @Query("SELECT * FROM MultimediaItem ORDER BY fileName DESC")
    Flowable<List<MultimediaItemsTags>> getAllItemsSortedByTitleDesc();

    @Transaction
    @Query("SELECT * FROM MultimediaItem ORDER BY creationDate")
    Flowable<List<MultimediaItemsTags>> getAllItemsSortedByCreationDateAsc();

    @Transaction
    @Query("SELECT * FROM MultimediaItem ORDER BY creationDate DESC")
    Flowable<List<MultimediaItemsTags>> getAllItemsSortedByCreationDateDesc();

    @Transaction
    @Query("SELECT *" +
            "FROM MultimediaItem M JOIN MultimediaItemTagCrossRefFTS MFTS " +
            "ON M.fileName=MFTS.fileName " +
            "WHERE MultimediaItemTagCrossRefFTS MATCH :query")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTags(String query);

    @Query("SELECT COUNT(*) FROM MultimediaItem WHERE fileName=:fileName")
    Single<Integer> numberOfItemsWithProvidedFileName(String fileName);

}
