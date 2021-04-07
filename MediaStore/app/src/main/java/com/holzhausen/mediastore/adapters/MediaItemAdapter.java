package com.holzhausen.mediastore.adapters;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.callbacks.DeleteItemSnackBarCallback;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaItemsTags;
import com.holzhausen.mediastore.model.MultimediaType;
import com.holzhausen.mediastore.model.Tag;
import com.holzhausen.mediastore.util.IAdapterHelper;
import com.holzhausen.mediastore.util.IViewHolderHelper;
import com.holzhausen.mediastore.util.ImageHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MediaItemAdapter extends RecyclerView.Adapter<MediaItemAdapter.ViewHolder> implements IViewHolderHelper {

    private List<MultimediaItemsTags> multimediaItems;

    private final Disposable disposable;

    private final IAdapterHelper<MultimediaItem> helper;

    private MultimediaItemsTags deletedItem;

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

    public MediaItemAdapter(Flowable<List<MultimediaItemsTags>> multimediaItems,
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
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultimediaType type = multimediaItems.get(position).getMultimediaItem().getMultimediaType();
        if(type == MultimediaType.IMAGE) {
            Bitmap preview = helper.readBitmapFromFile(multimediaItems.get(position).getMultimediaItem().getFilePath());
            holder.getPreview().setImageBitmap(preview);
            File imageFile = helper
                    .getContext()
                    .getFileStreamPath(multimediaItems.get(position).getMultimediaItem().getFilePath());
            Uri imageUri = FileProvider.getUriForFile(helper.getContext(), ImageHelper.FILE_PROVIDER_ACCESS,
                    imageFile);
            holder
                    .getPreview()
                    .setRotation(ImageHelper
                            .getImageOrientation(helper.getContext(), imageUri, imageFile.getAbsolutePath()));
        }
        else if (type == MultimediaType.VOICE_RECORDING) {
            holder.getPreview().setImageDrawable(ContextCompat.getDrawable(helper.getContext(), R.drawable.ic_baseline_music_video_24));
        }
        else if (type == MultimediaType.VIDEO) {
            File videoFile = helper
                    .getContext()
                    .getFileStreamPath(multimediaItems.get(position).getMultimediaItem().getFilePath());
            Uri videoUri = FileProvider.getUriForFile(helper.getContext(), ImageHelper.FILE_PROVIDER_ACCESS,
                    videoFile);
            setImageViewForVideo(videoUri, holder.getPreview());
        }
        holder
                .getMultimediaTypeIcon()
                .setImageResource(IMAGE_TYPE_ICONS
                        .get(multimediaItems
                        .get(position)
                                .getMultimediaItem()
                                .getMultimediaType()));
        holder
                .getLikeIcon()
                .setImageResource(multimediaItems.get(position).getMultimediaItem().isLiked() ?
                        R.drawable.ic_baseline_star_24 : R.drawable.ic_baseline_star_border_24);
        holder.getMultimediaTitle().setText(multimediaItems.get(position).getMultimediaItem().getFileName());
        holder
                .getMultimediaCreationDate()
                .setText(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.GERMANY)
                .format(multimediaItems.get(position).getMultimediaItem().getCreationDate()));
        List<Tag> tags = multimediaItems.get(position).getTags();
        for (int i=0;i<holder.getItemTagsChips().length;i++) {
            if(i<tags.size()){
                holder.getItemTagsChips()[i].setText(tags.get(i).getTagName());
                holder.getItemTagsChips()[i].setVisibility(View.VISIBLE);
            }
            else {
                holder.getItemTagsChips()[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return multimediaItems.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if(deletedItem != null) {
            helper.removeItem(deletedItem.getMultimediaItem());
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
        snackbar.addCallback(new DeleteItemSnackBarCallback(helper, deletedItem.getMultimediaItem()));
        snackbar.show();
    }

    @Override
    public void updateLikeStatus(int position) {
        MultimediaItem multimediaItem = multimediaItems.get(position).getMultimediaItem();
        multimediaItem.setLiked(!multimediaItem.isLiked());
        helper.updateItem(multimediaItem);
    }

    @Override
    public void viewFile(int position) {
        MultimediaItem multimediaItem = multimediaItems.get(position).getMultimediaItem();
        if(multimediaItem.getMultimediaType() == MultimediaType.IMAGE) {
            helper.viewImage(multimediaItem.getFilePath());
        }
        else {
            helper.playFile(multimediaItem.getFilePath());
        }

    }

    private void setImageViewForVideo(Uri uri, ImageView imagePreview){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(helper.getContext(), uri);
        Bitmap bitmap = retriever.getFrameAtTime();
        imagePreview.setImageBitmap(bitmap);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView preview;

        private final ImageView multimediaTypeIcon;

        private final ImageView likeIcon;

        private final TextView multimediaTitle;

        private final TextView multimediaCreationDate;

        private final Chip[] itemTagsChips;

        private final IViewHolderHelper viewHolderHelper;

        public ViewHolder(@NonNull View itemView, IViewHolderHelper viewHolderHelper) {
            super(itemView);

            preview = itemView.findViewById(R.id.preview_image);
            preview.setOnClickListener(this);
            multimediaTypeIcon = itemView.findViewById(R.id.multimedia_type);
            likeIcon = itemView.findViewById(R.id.like_icon);
            likeIcon.setOnClickListener(this);
            multimediaTitle = itemView.findViewById(R.id.multimedia_title);
            multimediaCreationDate = itemView.findViewById(R.id.multimedia_creation_date);
            itemTagsChips = Stream.of(
                    (Chip) itemView.findViewById(R.id.item_chip_1),
                    (Chip) itemView.findViewById(R.id.item_chip_2),
                    (Chip) itemView.findViewById(R.id.item_chip_3)
            ).toArray(Chip[]::new);
            this.viewHolderHelper = viewHolderHelper;
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

        public Chip[] getItemTagsChips() {
            return itemTagsChips;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == likeIcon.getId()){
                viewHolderHelper.updateLikeStatus(getAdapterPosition());
            }
            else if (v.getId() == preview.getId()) {
                viewHolderHelper.viewFile(getAdapterPosition());
            }
        }
    }



}
