package com.boxdotsize.boxdotsize_android.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boxdotsize.boxdotsize_android.BoxAnalyzeInteractor
import com.boxdotsize.boxdotsize_android.R
import com.boxdotsize.boxdotsize_android.databinding.FragmentPreviewBinding
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.DisposableContainer
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UniBoxSizeMeasureFragment : Fragment() {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding

    private val contract = ActivityResultContracts.RequestPermission()

    private var imageCapture: ImageCapture? = null
    private val observable = PublishSubject.create<Float>()

    private lateinit var cameraExecutor: ExecutorService

    private var interactor: BoxAnalyzeInteractor? = null

    private var disposable: Disposable? = null

    private var progressDialog: AlertDialog?=null

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

        val dialogView=LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress,null)
        progressDialog=AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val activityResultLauncher = registerForActivityResult(contract) { isGanted ->
            if (isGanted) {
                startCamera()
                //subscribeToSubject()
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
                    progressDialog?.dismiss()
                }

                override fun onError() {
                    binding?.tvBoxAnalyzeResult?.text = "이미지 분석 실패"
                    progressDialog?.dismiss()
                }
            }, { isParamsExist ->
                if (!isParamsExist) {
                    Toast.makeText(requireContext(),"테스트를 먼저 진행해주세요!",Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            },"Task1")

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etAnalyzeInterval?.visibility=View.GONE
        binding?.tbPreviewNaviagition?.title="#TASK 1"
        binding?.pvVideo?.setOnClickListener {
            takePhoto()
            progressDialog?.show()
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
        progressDialog=null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

}