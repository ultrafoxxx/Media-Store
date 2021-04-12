package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.util.ImageHelper;
import com.mukesh.imageproccessing.PhotoFilter;
import com.mukesh.imageproccessing.filters.Documentary;
import com.mukesh.imageproccessing.filters.Filter;
import com.mukesh.imageproccessing.filters.Grayscale;
import com.mukesh.imageproccessing.filters.Lomoish;
import com.mukesh.imageproccessing.filters.None;
import com.mukesh.imageproccessing.filters.Posterize;
import com.mukesh.imageproccessing.filters.Sepia;
import com.mukesh.imageproccessing.filters.Vignette;
import com.oginotihiro.cropview.CropView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EditPhotoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private GLSurfaceView editView;

    private Bitmap resultImage;


    private Uri uri;

    private final static List<Filter> filters = Stream.of(
            new Documentary(),
            new Vignette(),
            new Grayscale(),
            new Lomoish(),
            new Sepia(),
            new Posterize()
    ).collect(Collectors.toList());

    private PhotoFilter photoFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        final Spinner spinner = findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.filters_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        editView = findViewById(R.id.imageToEdit);
        uri = (Uri) getIntent().getExtras().get(getString(R.string.uri));
        resultImage = getOriginalImage(uri);
        final Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(this::onEditClicked);

        photoFilter = new PhotoFilter(editView, bitmap -> {
            resultImage = bitmap;
        });
    }

    private void onEditClicked(View view) {
        try {
            File image = ImageHelper.createImageFile(this);
            FileOutputStream outputStream = new FileOutputStream(image);
            resultImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.file_name), image.getName());
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        resultImage = getOriginalImage(uri);
        photoFilter.applyEffect(resultImage, filters.get(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}