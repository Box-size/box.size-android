package com.boxdotsize.boxdotsize_android

import android.Manifest
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boxdotsize.boxdotsize_android.databinding.FragmentTestBinding
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TestFragment : Fragment() {


    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val contract = ActivityResultContracts.RequestPermission()

    private var imageCapture: ImageCapture? = null
    private val observable = PublishSubject.create<Float>()

    private lateinit var cameraExecutor: ExecutorService

    private var interactor: TestInteractor? = null

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
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        interactor =
            TestInteractor(object : TestInteractor.OnTestResultResponseListener {
                override fun onResponse(isTestSuccess: Boolean) {
                    if (isTestSuccess) {
                        Toast.makeText(requireContext(), "테스트가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "테스트 실패. 다시 테스트해주세요.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onError() {
                    Toast.makeText(requireContext(), "테스트 실패. 다시 테스트해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTestStart.setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val fileName = "img_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    val resolver = requireContext().contentResolver
                    val imageBitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        MediaStore.Images.Media.getBitmap(resolver, savedUri)
                    } else {
                        val source = ImageDecoder.createSource(resolver, savedUri!!)
                        ImageDecoder.decodeBitmap(source)
                    }

                    val file = File(requireContext().cacheDir, fileName)
                    val fileOutputStream = FileOutputStream(file)
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.close()

                    val width = imageBitmap.width
                    val height = imageBitmap.height

                    val msg = "Photo capture succeeded: ${output.savedUri} $width $height"
                    interactor?.requestBoxAnalyze(file)
                    Log.d(TAG, msg)
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
                    //Log.d("focal",focalLength.toString())
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
                    it.setSurfaceProvider(binding.pvTestCameraPreview.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

}