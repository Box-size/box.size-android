package com.boxdotsize.boxdotsize_android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat

class PermissionManage{

    companion object{
        private const val REQUEST_CODE_PERMISSIONS=10
        private val REQUIRED_PERMISSIONS=mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply{
            if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private fun allPermissionsGrated(activity:Activity)=REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(activity,it)== PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(activity:Activity){
        activity.let{

            //onRequestPermissionsResult()
        }
    }

//    fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if(requestCode== PreviewFragment.REQUEST_CODE_PERMISSIONS){
//            if(allPermissionsGrated()){
//                startCamera()
//            }else{
//                Toast.makeText(requireContext(),"권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
//                //finish()
//            }
//        }
//    }
}