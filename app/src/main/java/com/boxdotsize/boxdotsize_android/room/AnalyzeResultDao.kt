package com.boxdotsize.boxdotsize_android.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.boxdotsize.boxdotsize_android.retrofit.Response

@Dao
interface AnalyzeResultDao {

    @Query("SELECT * FROM analyze_result")
    fun getAll():List<AnalyzeResult>


    @Insert
    fun addResult(result:AnalyzeResult)

    @Delete
    fun deleteResult(result:AnalyzeResult)
}