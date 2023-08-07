package com.boxdotsize.boxdotsize_android.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnalyzeResult::class], version = 1)
abstract class BoxDotSizeDatabase:RoomDatabase() {
    abstract fun analyzeResultDao():AnalyzeResultDao
}