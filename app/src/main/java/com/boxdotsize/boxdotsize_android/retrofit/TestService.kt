package com.boxdotsize.boxdotsize_android.retrofit

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TestService {

    @Multipart
    @POST("/api/test")
    fun requestTest(
        @Part image: MultipartBody.Part,
    ): Call<TestResponseDTO>
}