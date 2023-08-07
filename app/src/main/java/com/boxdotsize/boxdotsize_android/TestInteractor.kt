package com.boxdotsize.boxdotsize_android

import android.graphics.BitmapFactory
import android.util.Log
import com.boxdotsize.boxdotsize_android.retrofit.TestResponseDTO
import com.boxdotsize.boxdotsize_android.retrofit.TestService
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
                response.body()?.let {
                    if (it.status == 200) listener.onResponse(true)
                    else listener.onResponse(false, it.errorMessage.toString())
                }
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

}