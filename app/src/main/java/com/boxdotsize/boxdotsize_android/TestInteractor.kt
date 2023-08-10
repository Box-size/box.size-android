package com.boxdotsize.boxdotsize_android

import com.boxdotsize.boxdotsize_android.retrofit.Params
import com.boxdotsize.boxdotsize_android.room.DBManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

class TestInteractor(private val listener: OnTestResultResponseListener) {

    interface OnTestResultResponseListener {
        fun onResponse(isTestSuccess: Boolean, msg: String = "")

        fun onError()
    }

    fun requestCameraParamsAnalyze(
        file: File
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val res=analyze(file)
            val params = Params(params = res.params)
            DBManager.cameraParamDao.insertOrUpdate(params)
            withContext(Dispatchers.Main) {
                listener.onResponse(true, "message")//성공여부 ui로 전달
            }
        }
    }

    private fun analyze(file: File): AnalyzeResult{

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

        return AnalyzeResult(result,params)
    }

    data class AnalyzeResult(
        val result:Boolean,
        val params: String)

}

