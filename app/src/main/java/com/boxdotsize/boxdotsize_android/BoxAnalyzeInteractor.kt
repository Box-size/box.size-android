package com.boxdotsize.boxdotsize_android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.boxdotsize.boxdotsize_android.room.AnalyzeResult
import com.boxdotsize.boxdotsize_android.room.DBManager
import com.boxdotsize.boxdotsize_android.room.Params
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

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
                res = analyze(file, cameraParams!!)?:return@launch
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
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

    private suspend fun analyze(file: File, params: String): BoxSize? {
        //TODO 여기서 분석 시작

        // TODO: YOLO 함수 작성
        Log.d("Start", "Start")
        val detectResult = runObjectDetection(file)?:return null
        Log.d("runObject", "runObject")
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

        val xyxyJsonString = JSONArray(detectResult.xyxy).toString()

        //box.py 의 main 함수 호출
        Log.d(TAG, "파이썬 호출3 ")
        val result: String =
            pythonModule.callAttr("main", originalImageData, cropImageData, params, xyxyJsonString)
                .toString()
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

    private suspend fun runObjectDetection(
        file: File,
    ):BoxDetectResult?{
        return suspendCancellableCoroutine { continuation ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@suspendCancellableCoroutine
/*            val resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                640,
                640,
                true
            )*/
            Log.d("detector", "convert Bitmap")
            val image = InputImage.fromBitmap(bitmap, 0)
            Log.d("detector", "Input Image")
            val localModel = LocalModel.Builder()
                .setAssetFilePath("model.tflite")
                .build()
            Log.d("detector", "Load Model")
            // Multiple object detection in static images
            val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
                    .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    //.enableClassification()
                    .setClassificationConfidenceThreshold(0.5f)
                    .setMaxPerObjectLabelCount(3)
                    .build()
            Log.d("detector", "Set Option")
            val objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
            Log.d("detector", "Set ObjectDetector")
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    val xyxys = mutableListOf<List<Double>>()
                    val croppedFiles = mutableListOf<File>()
                    Log.d("detector", "Detect Success")
                    Log.d("detector", detectedObjects.size.toString())
                    if(detectedObjects.size<=0){
                        continuation.resume(null)
                    }
                    detectedObjects.forEachIndexed { index, detectedObject ->
                        val box = detectedObjects[index].boundingBox
                        val x = box.left
                        val y = box.top
                        val width = box.right - box.left
                        val height = box.bottom - box.top
                        val croppedBitmap = bitmap.crop(max(x-20,0), max(y-20,0), min(width+20,bitmap.width), min(height+20,bitmap.height))
                        val croppedFile = croppedBitmap?.toFile() ?: return@addOnSuccessListener // 정의 필요
                        croppedFiles.add(croppedFile)
                        xyxys.add(
                            listOf(
                                x.toDouble(),
                                y.toDouble(),
                                (x + width).toDouble(),
                                (y + height).toDouble()
                            )
                        )
                    }

                    val res = BoxDetectResult(file, croppedFiles[0], xyxys[0])
                    continuation.resume(res) // 결과를 반환
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)// 실패하면 예외를 반환
                }
        }
    }

    private fun debugPrint(detectedObjects: List<DetectedObject>) {
        detectedObjects.forEachIndexed { index, detectedObject ->
            val box = detectedObject.boundingBox

            Log.d(TAG, "Detected object: $index")
            Log.d(TAG, " trackingId: ${detectedObject.trackingId}")
            Log.d(TAG, " boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")
            detectedObject.labels.forEach {
                Log.d(TAG, " categories: ${it.text}")
                Log.d(TAG, " confidence: ${it.confidence}")
            }
        }
    }

    private fun Bitmap.crop(x: Int, y: Int, width: Int, height: Int): Bitmap? {
        return try {
            Bitmap.createBitmap(this, x, y, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun Bitmap.toFile(): File {
        val fileName = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val file = File(BoxDotSize.ApplicationContext().cacheDir, fileName)
        file.createNewFile()

        val bos = FileOutputStream(file)
        compress(Bitmap.CompressFormat.PNG, 0 , bos)
        bos.flush()
        bos.close()

        return file
    }

    fun getCameraParams(): LiveData<Params?> = DBManager.cameraParamDao.getCameraParams()

    data class BoxSize(val width: Float, val height: Float, val tall: Float)

}