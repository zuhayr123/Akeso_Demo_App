package com.example.akesodemoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.akesodemoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkCameraPermission()

        binding.grantAccess.setOnClickListener{
            checkCameraPermission()
            binding.grantAccess.visibility = View.GONE
            binding.circularProgressIndicator.visibility = View.VISIBLE
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            // Permission already granted, you can use the camera
            moveToNextActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    moveToNextActivity()
                } else {
                    binding.grantAccess.visibility = View.VISIBLE
                    binding.circularProgressIndicator.visibility = View.GONE
                    binding.textView3.text = "Oops! Looks like we could not get the permission, please try again!"
                    Log.e("TAG", "Permission not granted")
                }
                return
            }
            else -> {
                // Ignore all other requests
            }
        }
    }

    fun moveToNextActivity(){
        Log.e("TAG", "Moved to next activity")
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}