package com.boxdotsize.boxdotsize_android.room

import androidx.room.Room
import com.boxdotsize.boxdotsize_android.BoxDotSize

object DBManager {
    val db= Room.databaseBuilder(
        BoxDotSize.ApplicationContext(),
        BoxDotSizeDatabase::class.java,"box-analyze"
    ).build()

    val analyzeResultDao=db.analyzeResultDao()
    val cameraParamDao=db.cameraParamsDao()
}