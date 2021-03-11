package com.holzhausen.mediastore.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.adapters.MediaItemAdapter;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaType;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<MultimediaItem> multimediaItems = new LinkedList<>();
        multimediaItems.add(new MultimediaItem("photo", MultimediaType.IMAGE, true));
        multimediaItems.add(new MultimediaItem("video", MultimediaType.VIDEO, false));
        multimediaItems.add(new MultimediaItem("recording", MultimediaType.VOICE_RECORDING,
                false));
        multimediaItems.add(new MultimediaItem("second-photo", MultimediaType.IMAGE, false));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(new MediaItemAdapter(multimediaItems));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}