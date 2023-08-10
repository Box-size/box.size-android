package com.boxdotsize.boxdotsize_android.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boxdotsize.boxdotsize_android.retrofit.Params

@Dao
interface CameraParamsDao {

    @Query("SELECT * FROM camera_params WHERE id = 0")
    fun getCameraParams(): LiveData<Params?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(params: Params)
}