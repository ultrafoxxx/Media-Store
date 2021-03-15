package com.holzhausen.mediastore.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.callbacks.DeleteItemSnackBarCallback;
import com.holzhausen.mediastore.databases.IDBHelper;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaType;
import com.holzhausen.mediastore.util.IAdapterHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MediaItemAdapter extends RecyclerView.Adapter<MediaItemAdapter.ViewHolder> {

    private List<MultimediaItem> multimediaItems;

    private final Disposable disposable;

    private final IAdapterHelper<MultimediaItem> helper;

    private MultimediaItem deletedItem;

    private int deletedItemPosition;



    private static final Map<MultimediaType, Integer> IMAGE_TYPE_ICONS = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(
                    MultimediaType.IMAGE, R.drawable.ic_outline_image_24
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                    MultimediaType.VIDEO, R.drawable.ic_outline_video_library_24
            ),
            new AbstractMap.SimpleImmutableEntry<>(
                    MultimediaType.VOICE_RECORDING, R.drawable.ic_baseline_music_video_24
            )
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public MediaItemAdapter(Flowable<List<MultimediaItem>> multimediaItems,
                            final IAdapterHelper<MultimediaItem> helper) {
        this.multimediaItems = new LinkedList<>();
        disposable = multimediaItems
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
            this.multimediaItems = items;
            notifyDataSetChanged();
        });
        this.helper = helper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.multimedia_item, parent, false);
        return new ViewHolder(view, this::updateLikeStatus);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Bitmap preview = helper.readBitmapFromFile(multimediaItems.get(position).getFileName());
        if(preview != null) {
            holder.getPreview().setImageBitmap(preview);
        }
        holder
                .getMultimediaTypeIcon()
                .setImageResource(IMAGE_TYPE_ICONS
                        .get(multimediaItems
                        .get(position)
                                .getMultimediaType()));
        holder
                .getLikeIcon()
                .setImageResource(multimediaItems.get(position).isLiked() ?
                        R.drawable.ic_baseline_star_24 : R.drawable.ic_baseline_star_border_24);
        holder.getMultimediaTitle().setText(multimediaItems.get(position).getFileName());
        holder
                .getMultimediaCreationDate()
                .setText(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMANY)
                .format(multimediaItems.get(position).getCreationDate()));
    }

    @Override
    public int getItemCount() {
        return multimediaItems.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if(deletedItem != null) {
            helper.removeItem(deletedItem);
        }
        disposable.dispose();
    }

    public void removeItem(int position){
        deletedItemPosition = position;
        deletedItem = multimediaItems.get(position);
        showUndoSnackBar();
    }

    public void setDeletedItemToNull(){
        deletedItem = null;
    }

    private void showUndoSnackBar(){
        final Snackbar snackbar = Snackbar
                .make(helper.getView(R.id.main_activity_layout),
                "Undo deleting item?", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", view -> {
            multimediaItems.add(deletedItemPosition, deletedItem);
            notifyItemInserted(deletedItemPosition);
        });
        snackbar.addCallback(new DeleteItemSnackBarCallback(helper, deletedItem));
        snackbar.show();
    }

    private void updateLikeStatus(int position) {
        MultimediaItem multimediaItem = multimediaItems.get(position);
        multimediaItem.setLiked(!multimediaItem.isLiked());
        helper.updateItem(multimediaItem);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView preview;

        private final ImageView multimediaTypeIcon;

        private final ImageView likeIcon;

        private final TextView multimediaTitle;

        private final TextView multimediaCreationDate;

        private final Consumer<Integer> updateLikeStatusConsumer;

        public ViewHolder(@NonNull View itemView, Consumer<Integer> updateLikeStatusConsumer) {
            super(itemView);

            preview = itemView.findViewById(R.id.preview_image);
            multimediaTypeIcon = itemView.findViewById(R.id.multimedia_type);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeIcon.setOnClickListener(this);
            multimediaTitle = itemView.findViewById(R.id.multimedia_title);
            multimediaCreationDate = itemView.findViewById(R.id.multimedia_creation_date);
            this.updateLikeStatusConsumer = updateLikeStatusConsumer;
        }

        public ImageView getPreview() {
            return preview;
        }

        public ImageView getMultimediaTypeIcon() {
            return multimediaTypeIcon;
        }

        public ImageView getLikeIcon() {
            return likeIcon;
        }

        public TextView getMultimediaTitle() {
            return multimediaTitle;
        }

        public TextView getMultimediaCreationDate() {
            return multimediaCreationDate;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == likeIcon.getId()){
                updateLikeStatusConsumer.accept(getAdapterPosition());
            }
        }
    }

}
