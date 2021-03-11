package com.holzhausen.mediastore.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.adapters.MediaItemAdapter;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaListObservable;
import com.holzhausen.mediastore.model.MultimediaType;
import com.nambimobile.widgets.efab.FabOption;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SHOOT_IMAGE_REQUEST_CODE = 1;

    private static final int NAME_IMAGE_REQUEST_CODE = 2;

    private List<MultimediaItem> multimediaItems;

    private MediaItemAdapter mediaItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        multimediaItems = new LinkedList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mediaItemAdapter = new MediaItemAdapter(multimediaItems);
        recyclerView.setAdapter(mediaItemAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final FabOption photoOption = findViewById(R.id.new_photo_option);
        photoOption.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, SHOOT_IMAGE_REQUEST_CODE);
        });

        final MultimediaListObservable multimediaListObservable =
                new MultimediaListObservable(multimediaItems);
        multimediaListObservable.addObserver(mediaItemAdapter);
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
            multimediaItem.setPreview(filePreview);
            multimediaItems.add(multimediaItem);
            mediaItemAdapter.notifyDataSetChanged();
        }
    }
}