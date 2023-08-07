package com.boxdotsize.boxdotsize_android.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "analyze_result")
data class AnalyzeResult (
    @PrimaryKey(autoGenerate = true) val id:Int,
    val time:Date,
    val width:Float,
    val height:Float,
    val tall:Float
    )