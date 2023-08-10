package com.boxdotsize.boxdotsize_android

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.boxdotsize.boxdotsize_android.retrofit.BoxAnalyzeResponseDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.boxdotsize.boxdotsize_android.retrofit.BoxSizeAnalyzeService
import com.boxdotsize.boxdotsize_android.retrofit.Params
import com.boxdotsize.boxdotsize_android.room.DBManager
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import softeer.gogumac.slide.retrofit.RetrofitClient
import java.io.File
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

class BoxAnalyzeInteractor(private val listener: OnBoxAnalyzeResponseListener) {

    private var cameraParams: String? = null
    init {
        getCameraParams().observeForever {
            Toast.makeText(BoxDotSize.ApplicationContext(),"파라미터 가져옴",Toast.LENGTH_SHORT).show()
            cameraParams = it?.params
        }
    }

    private val service = RetrofitClient.getRetrofit().create(BoxSizeAnalyzeService::class.java)

    private var focalLength: Float? = null

    private val dummy = "{\n" +
            "      \"rvec\": [\n" +
            "        [\n" +
            "          0.0313322402852821\n" +
            "        ],\n" +
            "        [\n" +
            "          -0.004681713345008444\n" +
            "        ],\n" +
            "        [\n" +
            "          1.5518842368128438\n" +
            "        ]\n" +
            "      ],\n" +
            "      \"dist\": [\n" +
            "        [\n" +
            "          0.05691908804368303,\n" +
            "          -0.03639767315057699,\n" +
            "          0.0002100897003352741,\n" +
            "          -0.0031919270416946627,\n" +
            "          0.002166361341437373\n" +
            "        ]\n" +
            "      ],\n" +
            "      \"fx\": 1214.6697626787393,\n" +
            "      \"fy\": 1216.6780083151727,\n" +
            "      \"cx\": 1992.899518084872,\n" +
            "      \"cy\": 1469.4521814467491\n" +
            "    }"

    interface OnBoxAnalyzeResponseListener {
        fun onResponse(width: Float, height: Float, tall: Float)

        fun onError()
    }

    fun requestBoxAnalyze(
        file: File
    ) {

        //TODO 여기서 분석 시작
        //파이썬 코드 호출
        val python = Python.getInstance()
        //사용할 파이썬 파일에 box.py 등록
        val pythonModule = python.getModule("box")

        val imageData: ByteArray = file.readBytes()
        //box.py 의 main 함수 호출
        val result: String = pythonModule.callAttr("main", imageData, cameraParams).toString()
        //결과값 Json 객체화
        val resultJson = JSONObject(result)
        val width: Float = resultJson.getDouble("width").toFloat()
        val height: Float = resultJson.getDouble("height").toFloat()
        val tall: Float = resultJson.getDouble("tall").toFloat()
        //결과는 아래와 같이 반환( 넣으면 알아서 ui로 보내줘요~)
        listener.onResponse(width,height,tall)


//        this.focalLength ?: return
//        cameraParams ?: return
//        val body = file.toMultiPart()
//
////      val focalLength = RequestBody.create(MediaType.parse("text/plain"), focalLength.toString())
//        val params = RequestBody.create(MediaType.parse("text/plain"), cameraParams)
//        service.requestBoxSizeAnalyze(
//            body,
//            params
//        ).enqueue(object : Callback<BoxAnalyzeResponseDTO> {
//            override fun onResponse(
//                call: Call<BoxAnalyzeResponseDTO>,
//                response: Response<BoxAnalyzeResponseDTO>
//            ) {
//                Log.d("Retrofit", response.errorBody().toString())
//                response.body()?.response?.apply {
//                    listener.onResponse(
//                        this.width,
//                        this.height,
//                        this.tall
//                    )
//                }
//
//                if (response.body() != null) {
//
//                    Log.d("Retrofit", response.body()!!.response.tall.toString())
//                }
//            }
//
//            override fun onFailure(call: Call<BoxAnalyzeResponseDTO>, t: Throwable) {
//                Log.d("Retrofit", ",,,")
//            }
//        })
    }

    fun setFocalLength(focalLength: Float) {
        if (this.focalLength != null) return
        this.focalLength = focalLength
    }

    private fun File.toMultiPart(): MultipartBody.Part {
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), this)
        return MultipartBody.Part.createFormData("image", name, requestFile)
    }

    private fun getCameraParams(): LiveData<Params?> = DBManager.cameraParamDao.getCameraParams()

}