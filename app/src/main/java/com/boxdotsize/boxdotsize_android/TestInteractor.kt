package com.boxdotsize.boxdotsize_android

import android.graphics.BitmapFactory
import android.util.Log
import com.boxdotsize.boxdotsize_android.retrofit.Params
import com.boxdotsize.boxdotsize_android.retrofit.ParamsDTO
import com.boxdotsize.boxdotsize_android.retrofit.TestResponseDTO
import com.boxdotsize.boxdotsize_android.retrofit.TestService
import com.boxdotsize.boxdotsize_android.room.DBManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import softeer.gogumac.slide.retrofit.RetrofitClient
import java.io.File

class TestInteractor(private val listener: OnTestResultResponseListener) {

    interface OnTestResultResponseListener {
        fun onResponse(isTestSuccess: Boolean, msg: String = "")

        fun onError()
    }

    fun requestCameraParamsAnalyze(
        file: File
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            val result=analyze(file)
            val params = Params(params = result)
            DBManager.cameraParamDao.insertOrUpdate(params)
            withContext(Dispatchers.Main) {
                listener.onResponse(true, "message")//성공여부 ui로 전달
            }
        }

    }

    private fun analyze(file: File): String {
        //TODO 여기서 체커보드패턴 분석
        return "결과값 보내주세요~"
    }

}