package softeer.gogumac.slide.retrofit

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "http://52.79.88.247:8000/"
    private var retrofit: Retrofit? = null

    val loggingInterceptor = HttpLoggingInterceptor(object :HttpLoggingInterceptor.Logger{
        override fun log(message: String) {
            Log.d("OkHttp", message)
        }
    }).apply{
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }


    val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    fun getRetrofit(): Retrofit = retrofit ?: Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}