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

    private val service = RetrofitClient.getRetrofit().create(TestService::class.java)

    private var focalLength: Float? = null

    interface OnTestResultResponseListener {
        fun onResponse(isTestSuccess: Boolean, msg: String = "")

        fun onError()
    }

    fun requestBoxAnalyze(
        file: File
    ) {
        this.focalLength ?: return
        val body = file.toMultiPart()

        service.requestTest(
            body
        ).enqueue(object : Callback<TestResponseDTO> {
            override fun onResponse(
                call: Call<TestResponseDTO>,
                response: Response<TestResponseDTO>
            ) {
                Log.d("Retrofit", response.body().toString())
                if(response.body()!=null){
                    if (response.body()!!.status == 200) {

                        CoroutineScope(Dispatchers.IO).launch {
                            DBManager.cameraParamDao.insertOrUpdate(response.body()!!.response.params.toParams())
                            withContext(Dispatchers.Main) {
                                listener.onResponse(true)
                            }
                        }

                    }else listener.onResponse(false, response.body()!!.errorMessage.toString())
                }
                else listener.onResponse(false, "테스트 실패!")
            }

            override fun onFailure(call: Call<TestResponseDTO>, t: Throwable) {
                Log.d("Retrofit", "server communication fail")
                listener.onError()
            }
        })
    }

    fun setFocalLength(focalLength: Float) {
        if (this.focalLength != null) return
        this.focalLength = focalLength
    }

    private fun File.toMultiPart(): MultipartBody.Part {
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), this)
        return MultipartBody.Part.createFormData("image", name, requestFile)
    }

    private fun ParamsDTO.toParams(): Params {
        val jsonString = Gson().toJson(this)
        return Params(params = jsonString)
    }



}