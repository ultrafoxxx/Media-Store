package com.holzhausen.mediastore.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.adapters.MediaItemAdapter;
import com.holzhausen.mediastore.application.MediaStoreApp;
import com.holzhausen.mediastore.callbacks.SwipeDeleteItemCallback;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.databases.IDBHelper;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaType;
import com.holzhausen.mediastore.util.IAdapterHelper;
import com.nambimobile.widgets.efab.FabOption;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements IAdapterHelper<MultimediaItem> {

    private static final int SHOOT_IMAGE_REQUEST_CODE = 1;

    private static final int NAME_IMAGE_REQUEST_CODE = 2;

    private PublishSubject<List<MultimediaItem>> multimediaItemsSubject;

    private MediaItemAdapter mediaItemAdapter;

    private CompositeDisposable compositeDisposable;

    private MultimediaItemDao multimediaItemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        multimediaItemsSubject = PublishSubject.create();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mediaItemAdapter = new MediaItemAdapter(multimediaItemsSubject
                .toFlowable(BackpressureStrategy.BUFFER), this);
        recyclerView.setAdapter(mediaItemAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final FabOption photoOption = findViewById(R.id.new_photo_option);
        photoOption.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, SHOOT_IMAGE_REQUEST_CODE);
        });

        multimediaItemDao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .multimediaItemDao();

        compositeDisposable = new CompositeDisposable();

        Disposable disposable = multimediaItemDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    multimediaItemsSubject.onNext(items);
                }, error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                });
        compositeDisposable.add(disposable);

        final ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new SwipeDeleteItemCallback(mediaItemAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOOT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            final Bitmap takenPhoto = (Bitmap) data.getExtras().get("data");
            final Intent intent = new Intent(this, NameNewFileActivity.class);
            intent.putExtra("filePreview", takenPhoto);
            startActivityForResult(intent, NAME_IMAGE_REQUEST_CODE);
        }
        else if(requestCode == NAME_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            final Bitmap filePreview = (Bitmap) data.getExtras().get("filePreview");
            final String fileName = data.getStringExtra("fileTitle");
            final MultimediaItem multimediaItem = new MultimediaItem(fileName,
                    MultimediaType.IMAGE, false);
            insertItem(multimediaItem);
            saveBitmapToFile(filePreview, fileName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }


    @Override
    public void insertItem(MultimediaItem multimediaItem) {
        final Disposable disposable = multimediaItemDao
                .insert(multimediaItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        compositeDisposable.add(disposable);
    }

    @Override
    public void removeItem(MultimediaItem multimediaItem) {
        final Disposable disposable = multimediaItemDao
                .delete(multimediaItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    mediaItemAdapter.setDeletedItemToNull();
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public View getView(int resId) {
        return findViewById(resId);
    }

    @Override
    public Bitmap readBitmapFromFile(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveBitmapToFile(Bitmap bitmap, String fileName){
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Toast.makeText(this, "Problem saving image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



}