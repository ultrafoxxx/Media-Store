package com.holzhausen.mediastore.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.adapters.MediaItemAdapter;
import com.holzhausen.mediastore.application.MediaStoreApp;
import com.holzhausen.mediastore.callbacks.SwipeDeleteItemCallback;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.daos.MultimediaItemTagCrossRefDao;
import com.holzhausen.mediastore.daos.TagDao;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaItemTagCrossRef;
import com.holzhausen.mediastore.model.MultimediaItemsTags;
import com.holzhausen.mediastore.model.MultimediaType;
import com.holzhausen.mediastore.model.Tag;
import com.holzhausen.mediastore.util.IAdapterHelper;
import com.holzhausen.mediastore.util.ImageHelper;
import com.nambimobile.widgets.efab.FabOption;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements IAdapterHelper<MultimediaItem> {

    private static final int SHOOT_IMAGE_REQUEST_CODE = 1;

    private static final int NAME_IMAGE_REQUEST_CODE = 2;

    private static final int GALLERY_IMAGE_REQUEST_CODE = 3;

    private static final int NAME_IMAGE_FROM_GALLERY_REQUEST_CODE = 4;

    private static final int RECORD_VIDEO = 5;

    private static final int RECORD_VOICE = 6;

    private static final int NAME_VIDEO_REQUEST_CODE = 7;

    private static final int NAME_VOICE_REQUEST_CODE = 8;

    private PublishSubject<List<MultimediaItemsTags>> multimediaItemsSubject;

    private MediaItemAdapter mediaItemAdapter;

    private CompositeDisposable compositeDisposable;

    private MultimediaItemDao multimediaItemDao;

    private TagDao tagDao;

    private MultimediaItemTagCrossRefDao multimediaItemTagCrossRefDao;

    private String temporaryFileName;

    private Flowable<List<MultimediaItemsTags>> currentFlowable;

    private RecyclerView recyclerView;

    private FabOption photoOption;

    private FabOption galleryOption;

    private FabOption videoOption;

    private FabOption voiceOption;

    private TextView noElementsInfoView;

    private String currentQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        multimediaItemsSubject = PublishSubject.create();

        assignViews();
        prepareRecyclerView();
        assignListenersToViews();
        getDaos();

        compositeDisposable = new CompositeDisposable();

        queryMultimediaItems(multimediaItemDao.getAll());

    }

    private void getDaos() {
        multimediaItemDao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .multimediaItemDao();

        tagDao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .tagDao();

        multimediaItemTagCrossRefDao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .multimediaItemTagCrossRefDao();
    }

    private void assignListenersToViews() {
        photoOption.setOnClickListener(this::onShootPhotoClicked);
        galleryOption.setOnClickListener(this::onGetFromGalleryClicked);
        videoOption.setOnClickListener(this::onVideoRecordClicked);
        voiceOption.setOnClickListener(this::onVoiceRecordClicked);
    }

    private void prepareRecyclerView() {
        mediaItemAdapter = new MediaItemAdapter(multimediaItemsSubject
                .toFlowable(BackpressureStrategy.BUFFER), this);
        recyclerView.setAdapter(mediaItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        final ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new SwipeDeleteItemCallback(mediaItemAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void onVoiceRecordClicked(View view) {
        Intent takeRecordingIntent = new Intent(this, AudioRecordingActivity.class);
        startActivityForResult(takeRecordingIntent, RECORD_VOICE);
    }

    private void onVideoRecordClicked(View view) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, RECORD_VIDEO);
        }
    }

    private void onGetFromGalleryClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getString(R.string.image_mime));
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }

    private void onShootPhotoClicked(View view) {
        File image;
        try {
            image = ImageHelper.createImageFile(this);
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
            return;
        }
        Uri imageUri = FileProvider.getUriForFile(this,
                ImageHelper.FILE_PROVIDER_ACCESS, image);
        temporaryFileName = image.getName();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, SHOOT_IMAGE_REQUEST_CODE);
    }

    private void doAfterShootingPhoto(){
        final Intent intent = new Intent(this, NameNewFileActivity.class);
        fixRotation(getFileStreamPath(temporaryFileName));
        intent.putExtra(getString(R.string.file_name), temporaryFileName);
        startActivityForResult(intent, NAME_IMAGE_REQUEST_CODE);
    }

    private void doAfterNamingPhoto(Intent data){
        final String filePath = data.getStringExtra(getString(R.string.file_name));
        if(filePath != null){
            temporaryFileName = filePath;
        }
        insertNewMultimediaItem(temporaryFileName, data, MultimediaType.IMAGE);
    }

    private void doAfterTakingImageFromGallery(Intent data){
        final Uri uri = data.getData();
        final Intent intent = new Intent(this, NameNewFileActivity.class);
        intent.putExtra(getString(R.string.file_uri), uri);
        startActivityForResult(intent, NAME_IMAGE_FROM_GALLERY_REQUEST_CODE);
    }

    private void doAfterNamingImageFromGallery(Intent data) {
        final Uri uri = (Uri) data.getExtras().get(getString(R.string.uri));
        final String name = data.getStringExtra(getString(R.string.file_name));
        File image;
        if(name != null) {
            image = getFileStreamPath(name);
        } else {
            image = copyFile(uri);
        }
        insertNewMultimediaItem(image.getName(), data, MultimediaType.IMAGE);
    }

    private void startNamingActivityForRecordingOrVideo(int requestCode, Intent data){
        Uri uri = data.getData();
        temporaryFileName = data.getStringExtra(getString(R.string.file_name));
        final Intent intent = new Intent(this, NameNewFileActivity.class);
        intent.putExtra(getString(R.string.file_uri), uri);
        intent.putExtra(getString(R.string.is_image), false);
        intent.putExtra(getString(R.string.request_code), requestCode);
        startActivityForResult(intent, requestCode);
    }

    private void doAfterNamingVideo(Intent data) {
        final Uri uri = (Uri) data.getExtras().get(getString(R.string.uri));
        File video = copyFile(uri);
        insertNewMultimediaItem(video.getName(), data, MultimediaType.VIDEO);
    }

    private void doAfterNamingRecording(Intent data) {
        File recording = getFileStreamPath(temporaryFileName);
        insertNewMultimediaItem(recording.getName(), data, MultimediaType.VOICE_RECORDING);
    }

    private void insertNewMultimediaItem(String fileName, Intent data, MultimediaType multimediaType){
        final String fileTitle = data.getStringExtra(getString(R.string.file_title));
        final String[] fileTags = data.getStringArrayExtra(getString(R.string.file_tags));

        final MultimediaItem multimediaItem = new MultimediaItem(fileTitle, fileName,
                multimediaType, false);
        insertItem(multimediaItem);
        if(fileTags != null && fileTags.length > 0) {
            addTags(fileTags, multimediaItem);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOOT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            doAfterShootingPhoto();
        }
        else if(requestCode == NAME_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            doAfterNamingPhoto(data);
        }
        else if(requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            doAfterTakingImageFromGallery(data);
        }
        else if(requestCode == NAME_IMAGE_FROM_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            doAfterNamingImageFromGallery(data);
        }
        else if(requestCode == RECORD_VIDEO && resultCode == RESULT_OK && data != null) {
            startNamingActivityForRecordingOrVideo(NAME_VIDEO_REQUEST_CODE, data);
        }
        else if(requestCode == RECORD_VOICE && resultCode == RESULT_OK && data != null) {
            startNamingActivityForRecordingOrVideo(NAME_VOICE_REQUEST_CODE, data);
        }
        else if(requestCode == NAME_VIDEO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            doAfterNamingVideo(data);

        }
        else if(requestCode == NAME_VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            doAfterNamingRecording(data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sort_menu, menu);

        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                compositeDisposable.dispose();
                compositeDisposable = new CompositeDisposable();
                Flowable<List<MultimediaItemsTags>> multimediaItems = query.isEmpty() ?
                        multimediaItemDao.getAll() : multimediaItemDao.queryItemsByNamesAndTags(query);
                Disposable disposable = multimediaItems
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            multimediaItemsSubject.onNext(result);
                        });
                compositeDisposable.add(disposable);
                currentQuery = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if(hasFocus){
                return;
            }
            currentQuery = null;
            compositeDisposable.dispose();
            compositeDisposable = new CompositeDisposable();
            Disposable disposable = multimediaItemDao
                    .getAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        multimediaItemsSubject.onNext(result);
                    });
            compositeDisposable.add(disposable);
        });

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private boolean onStandardOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ascending_title:
                onSortClicked(multimediaItemDao.getAllItemsSortedByTitleAsc());
                return true;
            case R.id.descending_title:
                onSortClicked(multimediaItemDao.getAllItemsSortedByTitleDesc());
                return true;
            case R.id.ascending_date:
                onSortClicked(multimediaItemDao.getAllItemsSortedByCreationDateAsc());
                return true;
            case R.id.descending_date:
                onSortClicked(multimediaItemDao.getAllItemsSortedByCreationDateDesc());
                return true;
        }
        return false;
    }

    private boolean onOptionsItemSelectedInQueryMode(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ascending_title:
                onSortClicked(multimediaItemDao.queryItemsByNamesAndTagsOrderByNameAsc(currentQuery));
                return true;
            case R.id.descending_title:
                onSortClicked(multimediaItemDao.queryItemsByNamesAndTagsOrderByNameDesc(currentQuery));
                return true;
            case R.id.ascending_date:
                onSortClicked(multimediaItemDao.queryItemsByNamesAndTagsOrderByDateAsc(currentQuery));
                return true;
            case R.id.descending_date:
                onSortClicked(multimediaItemDao.queryItemsByNamesAndTagsOrderByDateDesc(currentQuery));
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(currentQuery == null) {
            return onStandardOptionsItemSelected(item);
        }
        else {
            return onOptionsItemSelectedInQueryMode(item);
        }
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
                    final Disposable internalDisposable = multimediaItemTagCrossRefDao
                            .delete(multimediaItem.getFileName())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                    compositeDisposable.add(internalDisposable);
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

    @Override
    public void viewImage(String fileName) {
        Uri imageUri = getFileUri(fileName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, getString(R.string.image_mime));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    public void playFile(String fileName) {
        Uri fileUri = getFileUri(fileName);
        Intent intent = new Intent(this, PlayActivity.class);
        intent.setData(fileUri);
        startActivity(intent);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void closeDBConnection() {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void queryMultimediaItems() {
        queryMultimediaItems(currentFlowable);
    }

    private Uri getFileUri(String fileName) {
        return FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS,
                getFileStreamPath(fileName));
    }

    private void deleteMediaFile(String filePath) {
        File file = getFileStreamPath(filePath);
        boolean deleted = file.delete();
        if(deleted) {
            Log.i("File", "file deleted");
        }
    }

    private File copyFile(Uri uri) {
        try {
            File image = ImageHelper.createImageFile(this);;
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileUtils.copyInputStreamToFile(inputStream, image);
            inputStream.close();
            return image;

        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }

    private void onSortClicked(Flowable<List<MultimediaItemsTags>> multimediaItems) {
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        queryMultimediaItems(multimediaItems);
    }

    private void queryMultimediaItems(Flowable<List<MultimediaItemsTags>> multimediaItems) {
        currentFlowable = multimediaItems;
        Disposable disposable = multimediaItems
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    multimediaItemsSubject.onNext(items);
                    if(items.isEmpty()) {
                        noElementsInfoView.setVisibility(View.VISIBLE);
                    }
                    else {
                        noElementsInfoView.setVisibility(View.GONE);
                    }
                }, error -> {
                    error.printStackTrace();
                    Toast.makeText(this, getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
                });
        compositeDisposable.add(disposable);
    }

    private void addTags(String[] tagNames, MultimediaItem newItem){

        Tag[] tags = Arrays.stream(tagNames).map(Tag::new).toArray(Tag[]::new);
        MultimediaItemTagCrossRef[] crossRefs = Arrays.stream(tagNames)
                .map(tagName -> new MultimediaItemTagCrossRef(newItem.getFileName(), tagName))
                .toArray(MultimediaItemTagCrossRef[]::new);

        final Disposable disposable = tagDao
                .insert(tags)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    final Disposable internalDisposable = multimediaItemTagCrossRefDao
                            .insert(crossRefs)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                    compositeDisposable.add(internalDisposable);

        });
        compositeDisposable.add(disposable);

    }

    private void fixRotation(File file) {

        Uri uri = FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS, file);
        Bitmap image = getOriginalImage(uri);
        Matrix matrix = new Matrix();
        matrix.postRotate(ImageHelper.getImageOrientation(this, uri, file.getAbsolutePath()));
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                image.getHeight(), matrix, true);

        try {
            OutputStream os = getContentResolver().openOutputStream(uri, "w");
            image.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private Bitmap getOriginalImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void assignViews() {
        recyclerView = findViewById(R.id.recyclerView);
        photoOption = findViewById(R.id.new_photo_option);
        galleryOption = findViewById(R.id.storage_option);
        videoOption = findViewById(R.id.new_video_option);
        voiceOption = findViewById(R.id.new_voice_option);
        noElementsInfoView = findViewById(R.id.no_elements_info);
    }


}