package com.holzhausen.mediastore.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.application.MediaStoreApp;
import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.util.ImageHelper;

import java.io.File;
import java.io.IOException;

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

    private final static int EDIT_PHOTO_REQUEST_CODE = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_new_file);


        imagePreview = findViewById(R.id.file_preview);
        fileName = getIntent().getStringExtra("fileName");
        uri = (Uri) getIntent().getExtras().get("fileUri");
        if(fileName != null){
            openImageByFileName(fileName);
            uri = FileProvider.getUriForFile(this, "com.holzhausen.mediastore.authority",
                    getFileStreamPath(fileName));
            imagePreview.setRotation(
                    ImageHelper
                            .getImageOrientation(this, uri,
                                    getFileStreamPath(fileName).getAbsolutePath()));
            originalFileName = fileName;

        }
        else if(uri != null){
            imagePreview.setImageURI(uri);
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            fileName = data.getStringExtra("fileName");
            uri = FileProvider.getUriForFile(this, "com.holzhausen.mediastore.authority",
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
        else if(properlyFinishedActivity && !originalFileName.equals(fileName)) {
            File image = new File(getFilesDir(), originalFileName);
            image.delete();
        }
        compositeDisposable.dispose();
    }

    private void openImageByFileName(String fileName){
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(openFileInput(fileName));
            imagePreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

}