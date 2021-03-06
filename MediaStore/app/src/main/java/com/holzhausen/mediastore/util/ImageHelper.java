package com.holzhausen.mediastore.util;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ImageHelper {

    public final static String FILE_PROVIDER_ACCESS = "com.holzhausen.mediastore.authority";

    private final static Map<Integer, Integer> orientations = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(ExifInterface.ORIENTATION_UNDEFINED, 0),
            new AbstractMap.SimpleImmutableEntry<>(ExifInterface.ORIENTATION_ROTATE_90, 90),
            new AbstractMap.SimpleImmutableEntry<>(ExifInterface.ORIENTATION_ROTATE_180, 180),
            new AbstractMap.SimpleImmutableEntry<>(ExifInterface.ORIENTATION_ROTATE_270, 270)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static int getImageOrientation(Context context, Uri imageUri, String filePath){

        context.getContentResolver().notifyChange(imageUri, null);
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            return orientations.get(orientation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = "Photo_" + timeStamp + "_";
        File storageDir = context.getFilesDir();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static File createRecordingFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = "Photo_" + timeStamp + "_";
        File storageDir = context.getFilesDir();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
    }

}
