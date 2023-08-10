package com.boxdotsize.boxdotsize_android.room

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    @TypeConverter
    fun localDateToTimestamp(localDate: LocalDate?): Long? {
        return localDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
}