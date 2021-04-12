package com.holzhausen.mediastore.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.application.MediaStoreApp;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.util.ImageHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NameNewFileActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable;
    
    private boolean properlyFinishedActivity;

    private String fileName;

    private String originalFileName;

    private ImageView imagePreview;

    private Uri uri;

    private Uri originalUri;

    private int visibleTags;

    private List<String> tagNames;

    private final static int EDIT_PHOTO_REQUEST_CODE = 9;

    private final static int CROP_PHOTO_REQUEST_CODE = 10;

    private static final int NAME_VIDEO_REQUEST_CODE = 7;

    private static final int NAME_VOICE_REQUEST_CODE = 8;

    private EditText titleInput;

    private Button submitButton;

    private Button editImageButton;

    private Button cropImageButton;

    private EditText tagInput;

    private Button addTagButton;

    private Chip[] tagChips;

    private MultimediaItemDao dao;

    private void assignViews() {
        imagePreview = findViewById(R.id.file_preview);
        titleInput = findViewById(R.id.title_text_input);
        submitButton = findViewById(R.id.set_title_button);
        editImageButton = findViewById(R.id.edit_photo_button);
        cropImageButton = findViewById(R.id.cropImageButton);
        tagInput = findViewById(R.id.tag_text_input);
        addTagButton = findViewById(R.id.add_tag_button);
        tagChips = new Chip[3];
        tagChips[0] = findViewById(R.id.chip_1);
        tagChips[1] = findViewById(R.id.chip_2);
        tagChips[2] = findViewById(R.id.chip_3);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_new_file);

        assignViews();
        fileName = getIntent().getStringExtra(getString(R.string.file_name));
        uri = (Uri) getIntent().getExtras().get(getString(R.string.file_uri));
        prepareFilePreview();
        originalUri = uri;

        dao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .multimediaItemDao();

        compositeDisposable = new CompositeDisposable();

        tagNames = new ArrayList<>();

        setListeners();

        boolean isImage = getIntent().getBooleanExtra(getString(R.string.is_image), true);
        if(!isImage){
            editImageButton.setEnabled(false);
            cropImageButton.setEnabled(false);
        }

    }

    private void setListeners() {
        submitButton.setOnClickListener(this::onSubmit);
        editImageButton.setOnClickListener(this::onEdit);
        cropImageButton.setOnClickListener(this::onCrop);
        addTagButton.setOnClickListener(this::onAddTags);
        setOnClickListenerForChips();
    }

    private void prepareFilePreview() {

        if(fileName != null){
            openImageByFileName(fileName);
            uri = FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS,
                    getFileStreamPath(fileName));
            originalFileName = fileName;

        }
        else if(uri != null){
            int requestCode = getIntent().getIntExtra(getString(R.string.request_code), 0);
            if(requestCode == NAME_VIDEO_REQUEST_CODE){
                setImageViewForVideo(uri);
            }
            else if(requestCode == NAME_VOICE_REQUEST_CODE) {
                imagePreview.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_music_video_24));
            }
            else {
                imagePreview.setImageURI(uri);
            }
        }
    }

    private void setOnClickListenerForChips() {
        for (Chip tagChip : tagChips) {
            tagChip.setOnCloseIconClickListener(view -> {
                visibleTags--;
                tagNames = tagNames
                        .stream()
                        .filter(tagName -> !tagName.contentEquals(((Chip)view).getText()))
                        .collect(Collectors.toList());
                tagChips[visibleTags].setVisibility(View.GONE);
                for(int i=0;i<tagNames.size();i++){
                    tagChips[i].setText(tagNames.get(i));
                }
            });
        }
    }

    private void onAddTags(View view) {
        final String tagValue = tagInput.getText().toString();
        if(tagValue == null || tagValue.isEmpty()){
            return;
        }
        if(visibleTags < tagChips.length) {
            Chip tagChip = tagChips[visibleTags];
            tagNames.add(tagValue);
            tagChip.setText(tagValue);
            tagChip.setVisibility(View.VISIBLE);
            visibleTags++;
        }
        tagInput.setText("");
    }

    private void onCrop(View view) {
        Intent intent = new Intent(this, CropPhotoActivity.class);
        intent.putExtra(getString(R.string.uri), uri);
        startActivityForResult(intent, CROP_PHOTO_REQUEST_CODE);
    }

    private void onEdit(View view) {
        Intent intent = new Intent(this, EditPhotoActivity.class);
        intent.putExtra(getString(R.string.uri), originalUri);
        startActivityForResult(intent, EDIT_PHOTO_REQUEST_CODE);
    }

    private void onSubmit(View view) {
        String fileTitle = titleInput.getText().toString();
        Disposable disposable = dao.numberOfItemsWithProvidedFileName(fileTitle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryResult -> {
                    if(queryResult > 0){
                        Toast.makeText(this,
                                getString(R.string.exists_warning),
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        finishThisActivity(fileTitle);
                    }
                }, error -> {
                    Toast.makeText(this, getString(R.string.wrong_info), Toast.LENGTH_SHORT)
                            .show();
                    error.printStackTrace();
                });
        compositeDisposable.add(disposable);
    }

    private void finishThisActivity(String fileTitle) {
        Intent result = new Intent();
        result.putExtra(getString(R.string.file_title), fileTitle);
        result.putExtra(getString(R.string.file_tags), tagNames.toArray(new String[visibleTags]));
        if(uri != null){
            result.putExtra(getString(R.string.uri), uri);
            if(!uri.equals(originalUri)){
                result.putExtra(getString(R.string.file_name), fileName);
            }
        }
        setResult(RESULT_OK, result);
        properlyFinishedActivity = true;
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            fileName = data.getStringExtra(getString(R.string.file_name));
            uri = FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS,
                    getFileStreamPath(fileName));
            imagePreview.setImageURI(uri);
        }
        else if(requestCode == CROP_PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if(!fileName.equals(originalFileName)){
                File file = getFileStreamPath(originalFileName);
                file.delete();
            }
            fileName = data.getStringExtra(getString(R.string.file_name));
            uri = data.getData();
            imagePreview.setImageURI(uri);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!properlyFinishedActivity && fileName != null){
            File image = new File(getFilesDir(), fileName);
            image.delete();
        }
        else if(properlyFinishedActivity && originalFileName!= null && !originalFileName.equals(fileName)) {
            File image = new File(getFilesDir(), originalFileName);
            image.delete();
        }
        compositeDisposable.dispose();
    }

    private void openImageByFileName(String fileName){
        try {
            FileInputStream fis = openFileInput(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            imagePreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageViewForVideo(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        imagePreview.setImageBitmap(bitmap);
    }

}