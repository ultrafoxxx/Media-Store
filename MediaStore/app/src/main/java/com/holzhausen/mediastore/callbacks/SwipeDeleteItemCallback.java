package com.holzhausen.mediastore.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.holzhausen.mediastore.adapters.MediaItemAdapter;

public class SwipeDeleteItemCallback extends ItemTouchHelper.SimpleCallback {

    private final MediaItemAdapter mediaItemAdapter;


    public SwipeDeleteItemCallback(final MediaItemAdapter mediaItemAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mediaItemAdapter = mediaItemAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mediaItemAdapter.removeItem(viewHolder.getAdapterPosition());
    }
}
