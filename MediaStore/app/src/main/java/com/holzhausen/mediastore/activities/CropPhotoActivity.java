package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.util.ImageHelper;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CropPhotoActivity extends AppCompatActivity {

    private Uri uri;

    private CropImageView cropView;

    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_photo);

        uri = (Uri) getIntent().getExtras().get(getString(R.string.uri));

        compositeDisposable = new CompositeDisposable();

        cropView = findViewById(R.id.cropImageView);
        cropView.setCropMode(CropImageView.CropMode.FREE);
        Disposable disposable = cropView.load(uri)
                .executeAsCompletable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        compositeDisposable.add(disposable);
        final Button editButton = findViewById(R.id.cropButton);
        editButton.setOnClickListener(this::onCropClicked);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    private void onCropClicked(View view) {
        try {
            final File cropImage = ImageHelper.createImageFile(this);
            final Uri saveUri = Uri.fromFile(cropImage);
            Disposable disposable = cropView.crop(uri)
                    .executeAsSingle()
                    .flatMap(bitmap -> cropView.save(bitmap).executeAsSingle(saveUri))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resultUri -> {
                        Intent intent = new Intent();
                        intent.setData(resultUri);
                        intent.putExtra(getString(R.string.file_name), cropImage.getName());
                        setResult(RESULT_OK, intent);
                        finish();
                    }, Throwable::printStackTrace);
            compositeDisposable.add(disposable);
        } catch (IOException e){
            e.printStackTrace();
        }


    }

}