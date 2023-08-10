package com.boxdotsize.boxdotsize_android.retrofit

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BoxSizeAnalyzeService {

    @Multipart
    @POST("/api/analyze-1")
    fun requestBoxSizeAnalyze(
        @Part image: MultipartBody.Part,
    @Part("params") params:okhttp3.RequestBody
    ):Call<BoxAnalyzeResponseDTO>

    @Multipart
    @POST("/api/analyze-2")
    fun requestMultiBoxSizeAnalyze(
        @Part image: MultipartBody.Part,
        @Part("width") originalWidth:okhttp3.RequestBody,
        @Part("height") originalHeight:okhttp3.RequestBody,
        @Part("focalLength") focalLength:okhttp3.RequestBody
    ):Call<BoxAnalyzeResponseDTO>

    @FormUrlEncoded
    @POST("/api/connection-test")
    fun test(
        @Field("width") width:Int=10,
        @Field("height") height:Int=10,
        @Field("focalLength") fl:Float=2f
    ):Call<BoxAnalyzeResponseDTO>
}