package com.holzhausen.mediastore.converters;

import androidx.room.TypeConverter;

import com.holzhausen.mediastore.model.MultimediaType;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String multimediaTypeToString(MultimediaType multimediaType){
        return multimediaType.name();
    }

    @TypeConverter
    public static MultimediaType stringToMultimediaType(String multimediaType){
        return MultimediaType.valueOf(multimediaType);
    }

}
