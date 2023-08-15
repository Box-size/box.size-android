package com.boxdotsize.boxdotsize_android.ui

import android.Manifest
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boxdotsize.boxdotsize_android.BoxAnalyzeInteractor
import com.boxdotsize.boxdotsize_android.databinding.FragmentPreviewBinding
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MultiBoxSizeMeasureFragment : Fragment() {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding

    private val contract = ActivityResultContracts.RequestPermission()

    private var imageCapture: ImageCapture? = null
    private val observable = PublishSubject.create<Float>()

    private lateinit var cameraExecutor: ExecutorService

    private var interactor: BoxAnalyzeInteractor? = null

    private var disposable: Disposable? = null

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    @ExperimentalCamera2Interop
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activityResultLauncher = registerForActivityResult(contract) { isGanted ->
            if (isGanted) {
                startCamera()
            }
        }

        activityResultLauncher.launch(Manifest.permission.CAMERA)
        cameraExecutor = Executors.newSingleThreadExecutor()
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        interactor =
            BoxAnalyzeInteractor(object : BoxAnalyzeInteractor.OnBoxAnalyzeResponseListener {
                override fun onResponse(width: Float, height: Float, tall: Float) {
                    val builder = StringBuilder().apply {
                        append("width : ")
                        append(width)
                        append("\nheight : ")
                        append(height)
                        append("\ntall : ")
                        append(tall)
                    }
                    binding?.tvBoxAnalyzeResult?.text = builder
                }

                override fun onError() {
                    binding?.tvBoxAnalyzeResult?.text = "이미지 분석 실패"
                }
            }, { isParamsExist ->
                if (!isParamsExist) {
                    Toast.makeText(requireContext(),"테스트를 먼저 진행해주세요!",Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            },"Task2")

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etAnalyzeInterval?.visibility=View.VISIBLE
        binding?.tbPreviewNaviagition?.title="#TASK 2"
        binding?.pvVideo?.setOnClickListener {
            subscribeToSubject()
        }
    }

    private fun subscribeToSubject() {
        val editText=binding?.etAnalyzeInterval?.text?:""
        val interval= if(editText.isEmpty())5000 else editText.toString().toLong()


        Toast.makeText(requireContext(),"${interval}ms 간격으로 촬영을 시작합니다.",Toast.LENGTH_SHORT).show()
        disposable = observable.throttleFirst(max(1000,interval), TimeUnit.MILLISECONDS)
            .subscribe {
                takePhoto()
            }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val cacheDir = requireContext().cacheDir

        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file=File(cacheDir,fileName)
        val outputOptions=ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    interactor?.requestBoxAnalyze(file)
                    try{
                        Toast.makeText(requireContext(), "이미지 분석", Toast.LENGTH_SHORT).show()
                    }catch (e:Exception){
                        Log.e(TAG,"not attached to a context.")
                    }

                }
            }
        )
    }

    @ExperimentalCamera2Interop
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    val focalLength = result.get(CaptureResult.LENS_FOCAL_LENGTH) ?: return
                    interactor?.setFocalLength(focalLength)
                    observable.onNext(focalLength)
                    super.onCaptureCompleted(session, request, result)
                }
            }
            val builder = Preview.Builder()

            Camera2Interop.Extender(builder).setSessionCaptureCallback(captureCallback)
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = builder
                .build()
                .also {
                    it.setSurfaceProvider(binding?.pvVideo?.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onStop() {
        super.onStop()
        interactor?.removeListeners()
        disposable?.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

}