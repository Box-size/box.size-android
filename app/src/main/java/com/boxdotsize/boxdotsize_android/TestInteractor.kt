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
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

class TestInteractor(private val listener: OnTestResultResponseListener) {

    private val service = RetrofitClient.getRetrofit().create(TestService::class.java)

    private var focalLength: Float? = null

    interface OnTestResultResponseListener {
        fun onResponse(isTestSuccess: Boolean, msg: String = "")

        fun onError()
    }

    fun requestCameraParamsAnalyze(
        file: File
    ) {

        //TODO 여기서 체커보드패턴 분석
        //파이썬 코드 호출
        val python = Python.getInstance()
        //사용할 파이썬 파일에 calibration.py 등록
        val pythonModule = python.getModule("calibration")

        val imageData: ByteArray = file.readBytes()
        //calibration.py 의 findParams 함수 호출
        val params : String = pythonModule.callAttr("findParams", imageData).toString()

        var result : Boolean = true
        val resultJson = JSONObject(params)
        //테스트 결과 실패면 false 반환
        if(resultJson.getDouble("fx").toFloat() == 0f &&
            resultJson.getDouble("fy").toFloat() == 0f &&
            resultJson.getDouble("cx").toFloat() == 0f &&
            resultJson.getDouble("cy").toFloat() == 0f){
            result = false
        }
        //결과는 아래와 같이 반 ( 넣으면 알아서 ui로 보내줘요~)
        listener.onResponse(result,"message") //성공여부, 메시지


//        this.focalLength ?: return
//        val body = file.toMultiPart()
//
//        service.requestTest(
//            body
//        ).enqueue(object : Callback<TestResponseDTO> {
//            override fun onResponse(
//                call: Call<TestResponseDTO>,
//                response: Response<TestResponseDTO>
//            ) {
//                Log.d("Retrofit", response.body().toString())
//                if(response.body()!=null){
//                    if (response.body()!!.status == 200) {
//
//                        CoroutineScope(Dispatchers.IO).launch {
//                            DBManager.cameraParamDao.insertOrUpdate(response.body()!!.response.params.toParams())
//                            withContext(Dispatchers.Main) {
//                                listener.onResponse(true)
//                            }
//                        }
//
//                    }else listener.onResponse(false, response.body()!!.errorMessage.toString())
//                }
//                else listener.onResponse(false, "테스트 실패!")
//            }
//
//            override fun onFailure(call: Call<TestResponseDTO>, t: Throwable) {
//                Log.d("Retrofit", "server communication fail")
//                listener.onError()
//            }
//        })
    }

    fun setFocalLength(focalLength: Float) {
        if (this.focalLength != null) return
        this.focalLength = focalLength
    }


}