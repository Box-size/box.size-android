package com.boxdotsize.boxdotsize_android

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.boxdotsize.boxdotsize_android.room.Params
import com.boxdotsize.boxdotsize_android.room.AnalyzeResult
import com.boxdotsize.boxdotsize_android.room.DBManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.lang.Exception

class BoxAnalyzeInteractor(private val listener: OnBoxAnalyzeResponseListener) {

    private var cameraParams: String? = null

    private val TAG = "BoxAnalyze"
    init {
        getCameraParams().observeForever {
            Toast.makeText(BoxDotSize.ApplicationContext(), "파라미터 가져옴", Toast.LENGTH_SHORT).show()
            cameraParams = it?.params
        }
    }

    private var focalLength: Float? = null

    interface OnBoxAnalyzeResponseListener {
        fun onResponse(width: Float, height: Float, tall: Float)

        fun onError()
    }

    fun requestBoxAnalyze(
        file: File
    ) {
        cameraParams ?: return
        CoroutineScope(Dispatchers.IO).launch {

            var res = BoxSize(0f, 0f, 0f)
            try {
                res = analyze(file, cameraParams!!)
            } catch (e: Exception) {
                Log.e(TAG , e.stackTraceToString())
                listener.onError()
                return@launch
            }

            val width = res.width
            val height = res.height
            val tall = res.tall

            if (width > 5 && height > 5 && tall > 5) {
                DBManager.analyzeResultDao.addResult(
                    AnalyzeResult(
                        0,
                        width = width,
                        height = height,
                        tall = tall,
                        url = file.path
                    )
                )
            }
            withContext(Dispatchers.Main) {
                listener.onResponse(width, height, tall)//성공여부 ui로 전달
            }
        }
    }

    private fun analyze(file: File, params: String): BoxSize {
        //TODO 여기서 분석 시작
        
        // TODO: YOLO 함수 작성
        val detectResult = detectBox(file)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(BoxDotSize.ApplicationContext()))
        }
        val python = Python.getInstance()
        Log.d(TAG, "파이썬 호출1")
        //사용할 파이썬 파일에 box.py 등록
        val pythonModule = python.getModule("box")
        Log.d(TAG, "파이썬 호출2")
        val originalImageData: ByteArray = detectResult.original.readBytes()
        val cropImageData: ByteArray = detectResult.crop.readBytes()
        //box.py 의 main 함수 호출
        Log.d(TAG, "파이썬 호출3 ")
        val result: String = pythonModule.callAttr("main", originalImageData, cropImageData, params, detectResult.xyxy).toString()
        Log.d(TAG, "파이썬 결과: $result")
        //결과값 Json 객체화
        val resultJson = JSONObject(result)
        val width: Float = resultJson.getDouble("width").toFloat()
        val height: Float = resultJson.getDouble("height").toFloat()
        val tall: Float = resultJson.getDouble("tall").toFloat()
        return BoxSize(width, height, tall)
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

    data class BoxSize(val width: Float, val height: Float, val tall: Float)

}