package com.example.akesodemoapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.akesodemoapp.databinding.ActivityCameraBinding
import com.example.midas.DepthEstimator
import com.example.midas.UtilityBItMapper

class CameraActivity: AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    lateinit var depthEstimator: DepthEstimator
    private var isBackCam = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)

        depthEstimator = DepthEstimator.Companion.Builder(this)
            .setFrameSkipInterval(2)  // Skip 2 frames
            .setQuality(1f)         // Set quality to maximum
            .build()

        setupToggleButtons()
        startCamera(CameraSelector.LENS_FACING_FRONT)

        binding.flipCamera.setOnClickListener {
            when( isBackCam ) {
                true -> startCamera(CameraSelector.LENS_FACING_BACK)
                false -> startCamera( CameraSelector.LENS_FACING_FRONT )
            }
            isBackCam = !isBackCam
        }
    }

    private fun startCamera(cameraDirection: Int) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        if (imageProxy.image != null) {
                            Log.e("VAl", "Value of shouldShowLiveFeed is $shouldShowLiveFeed")
                            if(shouldShowLiveFeed){
                                processImageFrame(imageProxy)
                            }
                            else{
                                imageProxy.close()
                            }
                        }
                    }
                }

            val cameraSelector = when (cameraDirection) {
                1 -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun processImageFrame(imageProxy: ImageProxy) {
        val frameBitmap = UtilityBItMapper.imageToBitmap(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        depthEstimator.estimateDepthAsync(frameBitmap) { depthBitmap ->
            binding.depthView.setImageBitmap(UtilityBItMapper.resizeBitmap(depthBitmap, frameBitmap.width, frameBitmap.height))
        }

        imageProxy.close()
    }

    private fun setupToggleButtons() {
        val button1 = binding.button1
        val button2 = binding.button2

        button1.setOnClickListener {
            shouldShowLiveFeed = false
            binding.depthView.visibility = View.GONE
        }
        button2.setOnClickListener {
            shouldShowLiveFeed = true
            binding.depthView.visibility = View.VISIBLE
        }
    }

    companion object {
        var shouldShowLiveFeed = true
        private const val TAG = "AkesoDemoApp"
    }
}