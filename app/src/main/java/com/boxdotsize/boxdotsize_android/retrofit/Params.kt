package com.boxdotsize.boxdotsize_android.retrofit

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson

@Entity("camera_params")
class Params(
    @PrimaryKey val id:Int=0,
    val params:String
)