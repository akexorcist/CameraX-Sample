package com.akexorcist.example.camerax.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.akexorcist.example.camerax.R
import com.akexorcist.example.camerax.databinding.ActivityVideoCaptureBinding
import com.akexorcist.example.camerax.helper.ShortenMultiplePermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport

class VideoCaptureActivity : AppCompatActivity() {
    private val binding: ActivityVideoCaptureBinding by lazy {
        ActivityVideoCaptureBinding.inflate(layoutInflater)
    }

    private var camera: Camera? = null
    private var videoCapture: VideoCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.buttonRecordVideo.setOnClickListener { onCaptureImage() }
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .withListener(multiplePermissionsListener)
            .check()
    }

    private fun setupCameraProvider() {
        ProcessCameraProvider.getInstance(this).also { provider ->
            provider.addListener({
                bindPreview(provider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()

        // TODO Wait until they make it public for implementation
//        videoCapture = VideoCapture.Builder()
//            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
        camera?.let {
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        }
    }

    private fun onCaptureImage() {
//        val file = File(filesDir.absoluteFile, "temp.mp4")
        // TODO Wait until they make it public for implementation
//        videoCapture?.startRecording(file, ContextCompat.getMainExecutor(this), videoSavedCallback)
    }

    private val multiplePermissionsListener = object : ShortenMultiplePermissionListener() {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
                onPermissionGrant()
            } else {
                onPermissionDenied()
            }
        }
    }

    // TODO Wait until they make it public for implementation
//    private val videoSavedCallback = object : VideoCapture.OnVideoSavedCallback {
//        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
//            showResultMessage(getString(R.string.video_record_success))
//        }
//
//        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
//            showResultMessage(getString(R.string.video_record_error, message, videoCaptureError))
//        }
//    }

    private fun onPermissionGrant() {
        setupCameraProvider()
    }

    private fun onPermissionDenied() {
        showResultMessage(getString(R.string.permission_denied))
        finish()
    }

    // TODO Wait until they make it public for implementation
//    private fun startVideoRecording() {
//        buttonToggleCamera.isEnabled = false
//        buttonCaptureImage.isEnabled = false
//        buttonRecordVideo.setText(R.string.stop_record_video)
//    }
//
//    private fun onStopVideoRecording() {
//        buttonToggleCamera.isEnabled = true
//        buttonCaptureImage.isEnabled = true
//        buttonRecordVideo.setText(R.string.start_record_video)
//    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
