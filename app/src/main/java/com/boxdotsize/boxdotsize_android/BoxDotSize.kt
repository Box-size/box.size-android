package com.boxdotsize.boxdotsize_android

import android.app.Application
import android.content.Context
import com.boxdotsize.boxdotsize_android.room.BoxDotSizeDatabase

class BoxDotSize: Application() {
    init{
        instance = this
    }

    companion object {
        lateinit var instance: BoxDotSize
        fun ApplicationContext() : Context {
            return instance.applicationContext
        }
    }

}