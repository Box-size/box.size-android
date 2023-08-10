package com.boxdotsize.boxdotsize_android.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.boxdotsize.boxdotsize_android.retrofit.Params

@Database(entities = [AnalyzeResult::class, Params::class], version = 1)
@TypeConverters(Converters::class)
abstract class BoxDotSizeDatabase:RoomDatabase() {
    abstract fun analyzeResultDao():AnalyzeResultDao
    abstract fun cameraParamsDao():CameraParamsDao
}