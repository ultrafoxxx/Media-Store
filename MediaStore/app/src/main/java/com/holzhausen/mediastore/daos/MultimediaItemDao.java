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
    Completable insert(MultimediaItem... multimediaItems);

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
            "FROM MultimediaItem M " +
            "WHERE M.fileName IN (SELECT MM.fileName " +
            "            FROM MultimediaItem MM JOIN MultimediaItemTagCrossRefFTS MFTS" +
            "            ON MM.fileName=MFTS.fileName" +
            "            WHERE MultimediaItemTagCrossRefFTS MATCH :query)")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTags(String query);

    @Transaction
    @Query("SELECT *" +
            "FROM MultimediaItem M " +
            "WHERE M.fileName IN (SELECT MM.fileName " +
            "            FROM MultimediaItem MM JOIN MultimediaItemTagCrossRefFTS MFTS" +
            "            ON MM.fileName=MFTS.fileName" +
            "            WHERE MultimediaItemTagCrossRefFTS MATCH :query)" +
            "            ORDER BY creationDate")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTagsOrderByDateAsc(String query);

    @Transaction
    @Query("SELECT *" +
            "FROM MultimediaItem M " +
            "WHERE M.fileName IN (SELECT MM.fileName " +
            "            FROM MultimediaItem MM JOIN MultimediaItemTagCrossRefFTS MFTS" +
            "            ON MM.fileName=MFTS.fileName" +
            "            WHERE MultimediaItemTagCrossRefFTS MATCH :query)" +
            "            ORDER BY creationDate DESC")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTagsOrderByDateDesc(String query);

    @Transaction
    @Query("SELECT *" +
            "FROM MultimediaItem M " +
            "WHERE M.fileName IN (SELECT MM.fileName " +
            "            FROM MultimediaItem MM JOIN MultimediaItemTagCrossRefFTS MFTS" +
            "            ON MM.fileName=MFTS.fileName" +
            "            WHERE MultimediaItemTagCrossRefFTS MATCH :query)" +
            "            ORDER BY fileName")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTagsOrderByNameAsc(String query);

    @Transaction
    @Query("SELECT *" +
            "FROM MultimediaItem M " +
            "WHERE M.fileName IN (SELECT MM.fileName " +
            "            FROM MultimediaItem MM JOIN MultimediaItemTagCrossRefFTS MFTS" +
            "            ON MM.fileName=MFTS.fileName" +
            "            WHERE MultimediaItemTagCrossRefFTS MATCH :query)" +
            "            ORDER BY fileName DESC")
    Flowable<List<MultimediaItemsTags>> queryItemsByNamesAndTagsOrderByNameDesc(String query);

    @Query("SELECT COUNT(*) FROM MultimediaItem WHERE fileName=:fileName")
    Single<Integer> numberOfItemsWithProvidedFileName(String fileName);

}
