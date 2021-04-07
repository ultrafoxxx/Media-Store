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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_new_file);


        imagePreview = findViewById(R.id.file_preview);
        fileName = getIntent().getStringExtra("fileName");
        uri = (Uri) getIntent().getExtras().get("fileUri");
        if(fileName != null){
            openImageByFileName(fileName);
            uri = FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS,
                    getFileStreamPath(fileName));
            imagePreview.setRotation(
                    ImageHelper
                            .getImageOrientation(this, uri,
                                    getFileStreamPath(fileName).getAbsolutePath()));
            originalFileName = fileName;

        }
        else if(uri != null){
            int requestCode = getIntent().getIntExtra("requestCode", 0);
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
        originalUri = uri;
        final EditText titleInput = findViewById(R.id.title_text_input);
        final Button submitButton = findViewById(R.id.set_title_button);

        final MultimediaItemDao dao = ((MediaStoreApp)getApplication())
                .getDatabase()
                .multimediaItemDao();

        compositeDisposable = new CompositeDisposable();

        submitButton.setOnClickListener(view -> {
            String fileTitle = titleInput.getText().toString();
            Disposable disposable = dao.numberOfItemsWithProvidedFileName(fileTitle)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(queryResult -> {
                        if(queryResult > 0){
                            Toast.makeText(this,
                                    "This file name already exists in database",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent result = new Intent();
                            result.putExtra("fileTitle", fileTitle);
                            result.putExtra("fileTags", tagNames.toArray(new String[visibleTags]));
                            if(uri != null){
                                result.putExtra("uri", uri);
                                if(!uri.equals(originalUri)){
                                    result.putExtra("fileName", fileName);
                                }
                            }
                            setResult(RESULT_OK, result);
                            properlyFinishedActivity = true;
                            finish();
                        }
                    }, error -> {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT)
                                .show();
                        error.printStackTrace();
                    });
            compositeDisposable.add(disposable);
        });

        final Button editImageButton = findViewById(R.id.edit_photo_button);
        editImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditPhotoActivity.class);
            intent.putExtra("uri", originalUri);
            if(fileName != null) {
                intent.putExtra("fileName", fileName);
            }
            startActivityForResult(intent, EDIT_PHOTO_REQUEST_CODE);

        });

        final Button cropImageButton = findViewById(R.id.cropImageButton);
        cropImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CropPhotoActivity.class);
            intent.putExtra("uri", originalUri);
            if(fileName != null) {
                intent.putExtra("fileName", fileName);
            }
            startActivityForResult(intent, CROP_PHOTO_REQUEST_CODE);

        });

        final EditText tagInput = findViewById(R.id.tag_text_input);
        final Button addTagButton = findViewById(R.id.add_tag_button);
        final Chip[] tagChips = {
                findViewById(R.id.chip_1),
                findViewById(R.id.chip_2),
                findViewById(R.id.chip_3)
        };
        tagNames = new ArrayList<>();
        addTagButton.setOnClickListener(view -> {
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
        });

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

        boolean isImage = getIntent().getBooleanExtra("isImage", true);
        if(!isImage){
            editImageButton.setVisibility(View.INVISIBLE);
            cropImageButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == EDIT_PHOTO_REQUEST_CODE || requestCode == CROP_PHOTO_REQUEST_CODE)
                && resultCode == RESULT_OK) {
            fileName = data.getStringExtra("fileName");
            uri = FileProvider.getUriForFile(this, ImageHelper.FILE_PROVIDER_ACCESS,
                    getFileStreamPath(fileName));
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
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageViewForVideo(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        imagePreview.setImageBitmap(bitmap);
    }

}