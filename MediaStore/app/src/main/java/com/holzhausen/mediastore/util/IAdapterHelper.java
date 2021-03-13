package com.holzhausen.mediastore.util;

import android.graphics.Bitmap;
import android.view.View;

import com.holzhausen.mediastore.databases.IDBHelper;

public interface IAdapterHelper<T> extends IDBHelper<T> {

    View getView(int resId);

    Bitmap readBitmapFromFile(String fileName);

}
