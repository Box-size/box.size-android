package com.boxdotsize.boxdotsize_android

import android.graphics.BitmapFactory
import android.util.Log
import com.boxdotsize.boxdotsize_android.retrofit.BoxAnalyzeResponseDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.boxdotsize.boxdotsize_android.retrofit.BoxSizeAnalyzeService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import softeer.gogumac.slide.retrofit.RetrofitClient
import java.io.File

class BoxAnalyzeInteractor(private val listener: OnBoxAnalyzeResponseListener) {

    private val service = RetrofitClient.getRetrofit().create(BoxSizeAnalyzeService::class.java)

    private var focalLength: Float? = null

    interface OnBoxAnalyzeResponseListener {
        fun onResponse(width: Float, height: Float, tall: Float)

        fun onError()
    }

    fun requestBoxAnalize(
        file: File
    ) {
        this.focalLength ?: return
        val body = file.toMultiPart()
        val size = getImageSize(file) ?: return

        val width = RequestBody.create(MediaType.parse("text/plain"), size.first.toString())
        val height = RequestBody.create(MediaType.parse("text/plain"), size.second.toString())
        val focalLength = RequestBody.create(MediaType.parse("text/plain"), focalLength.toString())

        service.requestBoxSizeAnalyze(
            body,
            width,
            height,
            focalLength
        ).enqueue(object : Callback<BoxAnalyzeResponseDTO> {
            override fun onResponse(
                call: Call<BoxAnalyzeResponseDTO>,
                response: Response<BoxAnalyzeResponseDTO>
            ) {
                Log.d("Retrofit", response.errorBody().toString())
                response.body()?.response?.apply {
                    listener.onResponse(
                        this.width,
                        this.height,
                        this.tall
                    )
                }

                if (response.body() != null) {

                    Log.d("Retrofit", response.body()!!.response.tall.toString())
                }
            }

            override fun onFailure(call: Call<BoxAnalyzeResponseDTO>, t: Throwable) {
                Log.d("Retrofit", ",,,")
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

    private fun getImageSize(file: File): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(file.absolutePath, options)

        Log.d("check", " ${options.outWidth}  ${options.outHeight}")
        if (options.outWidth != -1 && options.outHeight != -1) {
            return Pair(options.outWidth, options.outHeight)
        }

        return null
    }
}