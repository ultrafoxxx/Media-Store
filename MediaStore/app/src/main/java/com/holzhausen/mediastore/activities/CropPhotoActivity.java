package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.util.ImageHelper;
import com.oginotihiro.cropview.CropView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CropPhotoActivity extends AppCompatActivity {

    private Uri uri;

    private Bitmap resultImage;

    private CropView cropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_photo);

        uri = (Uri) getIntent().getExtras().get("uri");
        resultImage = getOriginalImage(uri);

        cropView = findViewById(R.id.cropView);
        cropView.of(uri).initialize(this);

        final Button editButton = findViewById(R.id.cropButton);
        editButton.setOnClickListener(this::onCropClicked);
    }

    private void onCropClicked(View view) {
        try {
            File image = ImageHelper.createImageFile(this);
            FileOutputStream outputStream = new FileOutputStream(image);
            resultImage = cropView.getOutput();
            resultImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Intent intent = new Intent();
            intent.putExtra("fileName", image.getName());
            setResult(RESULT_OK, intent);
            finish();
        } catch (IOException e) {
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
}