package com.boxdotsize.boxdotsize_android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.boxdotsize.boxdotsize_android.room.AnalyzeResult
import com.boxdotsize.boxdotsize_android.room.DBManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordViewModel: ViewModel() {

    val records= DBManager.analyzeResultDao.getAll()
    val changedRecord=MutableLiveData<List<AnalyzeResult>>()


    fun deleteAll(){
        CoroutineScope(Dispatchers.IO).launch {
            DBManager.analyzeResultDao.deleteAll()

            withContext(Dispatchers.Main) {
                changedRecord.value= listOf()
            }
        }

    }
}