package com.boxdotsize.boxdotsize_android

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.video.VideoCapture
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.boxdotsize.boxdotsize_android.databinding.FragmentPreviewBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PreviewFragment:Fragment() {

    private var _binding: FragmentPreviewBinding?=null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture?=null
    private var videoCapture:VideoCapture<Recorder>? =null
    private var recording: Recording?=null

    private lateinit var cameraExecutor:ExecutorService

    companion object{
        private const val TAG="CameraXApp"
        private const val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS=10
        private val REQUIRED_PERMISSIONS=mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply{
            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(allPermissionsGrated()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(requireActivity(),REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor= Executors.newSingleThreadExecutor()

        _binding= FragmentPreviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun takePhoto(){}

    private fun captureVideo(){}

    private fun startCamera(){
        val cameraPrividerFuture=ProcessCameraProvider.getInstance(requireContext())
        cameraPrividerFuture.addListener({
            val cameraProvider:ProcessCameraProvider=cameraPrividerFuture.get()
            val preview= Preview.Builder()
                .build()
                .also{
                    it.setSurfaceProvider(binding.pvVideo.surfaceProvider)
                }

            val cameraSelector= CameraSelector.DEFAULT_BACK_CAMERA

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,cameraSelector,preview
                )
            }catch(e:Exception){
                Log.e(TAG,"Use case binding filed",e)
            }
        },ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGrated()=REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(requireContext(),it)== PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGrated()){
                startCamera()
            }else{
                Toast.makeText(requireContext(),"권한을 허용해주세요.",Toast.LENGTH_SHORT).show()
                //finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding=null
    }
}