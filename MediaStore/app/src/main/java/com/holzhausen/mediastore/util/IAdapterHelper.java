package com.holzhausen.mediastore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.holzhausen.mediastore.databases.IDBHelper;

public interface IAdapterHelper<T> extends IDBHelper<T> {

    View getView(int resId);

    Bitmap readBitmapFromFile(String filePath);

    void viewImage(String fileName);

    void playFile(String fileName);

    Context getContext();

}
