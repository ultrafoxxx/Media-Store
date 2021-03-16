package com.holzhausen.mediastore.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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

    private static final int GALLERY_IMAGE_REQUEST_CODE = 3;

    private static final int NAME_IMAGE_FROM_GALLERY_REQUEST_CODE = 4;

    private static final String IMAGE_SHORTCUT = ".png";

    private PublishSubject<List<MultimediaItem>> multimediaItemsSubject;

    private MediaItemAdapter mediaItemAdapter;

    private CompositeDisposable compositeDisposable;

    private MultimediaItemDao multimediaItemDao;

    private String temporaryFileName;

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
            File image;
            try {
                image = createImageFile();
            } catch (IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri imageUri = FileProvider.getUriForFile(this,
                    "com.holzhausen.mediastore.authority", image);
            temporaryFileName = image.getName();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, SHOOT_IMAGE_REQUEST_CODE);
        });

        final FabOption galleryOption = findViewById(R.id.storage_option);
        galleryOption.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
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
            final Intent intent = new Intent(this, NameNewFileActivity.class);
            intent.putExtra("fileName", temporaryFileName);
            startActivityForResult(intent, NAME_IMAGE_REQUEST_CODE);
        }
        else if(requestCode == NAME_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final String fileName = data.getStringExtra("fileTitle");
            final MultimediaItem multimediaItem = new MultimediaItem(fileName, temporaryFileName,
                    MultimediaType.IMAGE, false);
            insertItem(multimediaItem);
        }
        else if(requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final Uri uri = data.getData();
            final Intent intent = new Intent(this, NameNewFileActivity.class);
            intent.putExtra("fileUri", uri);
            startActivityForResult(intent, NAME_IMAGE_FROM_GALLERY_REQUEST_CODE);
        }
        else if(requestCode == NAME_IMAGE_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final String fileName = data.getStringExtra("fileTitle");
            final Uri uri = (Uri) data.getExtras().get("uri");
            File image = copyFile(uri);
            final MultimediaItem multimediaItem = new MultimediaItem(fileName, image.getName(),
                    MultimediaType.IMAGE, false);
            insertItem(multimediaItem);
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
                    deleteMediaFile(multimediaItem.getFilePath());
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void updateItem(MultimediaItem item) {
        final Disposable disposable = multimediaItemDao
                .update(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        compositeDisposable.add(disposable);
    }

    @Override
    public View getView(int resId) {
        return findViewById(resId);
    }

    @Override
    public Bitmap readBitmapFromFile(String filePath) {
        try {
            FileInputStream fis = openFileInput(filePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteMediaFile(String filePath) {
        File file = getFileStreamPath(filePath);
        boolean deleted = file.delete();
        if(deleted) {
            Log.i("File", "file deleted");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = "Photo_" + timeStamp + "_";
        File storageDir = getFilesDir();
        return File.createTempFile(
                imageFileName,  /* prefix */
                IMAGE_SHORTCUT,         /* suffix */
                storageDir      /* directory */
        );
    }

    private File copyFile(Uri uri) {
        try {
            File image = createImageFile();
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileUtils.copyInputStreamToFile(inputStream, image);
            return image;

        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }


}