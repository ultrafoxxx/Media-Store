package com.holzhausen.mediastore.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.holzhausen.mediastore.R;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MediaItemAdapter extends RecyclerView.Adapter<MediaItemAdapter.ViewHolder>{

    private final List<MultimediaItem> multimediaItems;

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

    public MediaItemAdapter(List<MultimediaItem> multimediaItems) {
        this.multimediaItems = multimediaItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.multimedia_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            final Drawable image = Drawable.createFromStream(
                    holder.itemView
                            .getContext()
                            .getAssets()
                            .open(multimediaItems.get(position).getFileName()), null);
            holder.getPreview().setImageDrawable(image);
        } catch (IOException e) {
            e.printStackTrace();
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


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView preview;

        private final ImageView multimediaTypeIcon;

        private final ImageView likeIcon;

        private final TextView multimediaTitle;

        private final TextView multimediaCreationDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            preview = itemView.findViewById(R.id.preview_image);
            multimediaTypeIcon = itemView.findViewById(R.id.multimedia_type);
            likeIcon = itemView.findViewById(R.id.like_icon);
            multimediaTitle = itemView.findViewById(R.id.multimedia_title);
            multimediaCreationDate = itemView.findViewById(R.id.multimedia_creation_date);
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
    }

}
