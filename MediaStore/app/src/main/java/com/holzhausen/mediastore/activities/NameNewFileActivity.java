package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.application.MediaStoreApp;
import com.holzhausen.mediastore.daos.MultimediaItemDao;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NameNewFileActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable;
    
    private boolean shouldFinishActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_new_file);

        final Bitmap filePreview = (Bitmap) getIntent().getExtras().get("filePreview");
        final ImageView imageView = findViewById(R.id.file_preview);
        imageView.setImageBitmap(filePreview);

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
                            result.putExtra("filePreview", filePreview);
                            result.putExtra("fileTitle", fileTitle);
                            setResult(RESULT_OK, result);
                            finish();
                        }
                    }, error -> {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT)
                                .show();
                        error.printStackTrace();
                    });
            compositeDisposable.add(disposable);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}