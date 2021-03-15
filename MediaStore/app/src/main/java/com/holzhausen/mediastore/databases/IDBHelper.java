package com.holzhausen.mediastore.databases;

import io.reactivex.disposables.Disposable;

public interface IDBHelper<T> {

    void insertItem(T item);

    void removeItem(T item);

    void updateItem(T item);

}
