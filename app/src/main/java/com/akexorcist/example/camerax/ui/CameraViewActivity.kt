package com.akexorcist.example.camerax.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import com.akexorcist.example.camerax.R
import com.akexorcist.example.camerax.databinding.ActivityCameraViewBinding
import com.akexorcist.example.camerax.helper.ShortenMultiplePermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import java.io.File

class CameraViewActivity : AppCompatActivity() {
    private val binding: ActivityCameraViewBinding by lazy {
        ActivityCameraViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupView()
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .withListener(multiplePermissionsListener)
            .check()
    }

    private fun setupView() {
        binding.buttonToggleCamera.setOnClickListener { onToggleCameraClick() }
        binding.buttonCaptureImage.setOnClickListener { onCaptureImageClick() }
        binding.buttonRecordVideo.setOnClickListener { onRecordVideoClick() }
        // Currently, there's no zoom and camera bound listener supported for CameraView
//         seekBarZoom.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    @SuppressLint("MissingPermission")
    private fun bindCamera() {
        binding.cameraView.bindToLifecycle(this)
        binding.cameraView.isPinchToZoomEnabled = true
        // Currently, there's no zoom and camera bound listener supported for CameraView
//         seekBarZoom.max = ((cameraView.maxZoomRatio - cameraView.minZoomRatio) * 10).toInt()
//         seekBarZoom.progress = (cameraView.zoomRatio * 10).toInt()
    }

    private fun onToggleCameraClick() {
        binding.cameraView.toggleCamera()
    }

    private fun onCaptureImageClick() {
        val file = File(filesDir.absoluteFile, "temp.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        binding.cameraView.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), imageSavedCallback)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun onRecordVideoClick() {
        if (binding.cameraView.isRecording) {
            binding.cameraView.stopRecording()
            onStopVideoRecording()
        } else {
            startVideoRecording()
            val file = File(filesDir.absoluteFile, "temp.mp4")
            binding.cameraView.startRecording(file, ContextCompat.getMainExecutor(this), videoSavedCallback)
        }
    }

    private fun onPermissionGrant() {
        bindCamera()
    }

    private fun onPermissionDenied() {
        showResultMessage(getString(R.string.permission_denied))
        finish()
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

    private val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            showResultMessage(getString(R.string.image_capture_success))
        }

        override fun onError(exception: ImageCaptureException) {
            showResultMessage(getString(R.string.image_capture_error, exception.message, exception.imageCaptureError))
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private val videoSavedCallback = object : OnVideoSavedCallback {
        override fun onVideoSaved(outputFileResults: OutputFileResults) {
            showResultMessage(getString(R.string.video_record_success))
        }

        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
            showResultMessage(getString(R.string.video_record_error, message, videoCaptureError))
        }
    }

    // Currently, there's no zoom and camera bound listener supported for CameraView
//    private val seekBarChangeListener = object : ShortenSeekBarChangeListener() {
//        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//            cameraView.zoomRatio = progress / 10f
//        }
//    }

    private fun startVideoRecording() {
        binding.buttonToggleCamera.isEnabled = false
        binding.buttonCaptureImage.isEnabled = false
        binding.buttonRecordVideo.setText(R.string.stop_record_video)
    }

    private fun onStopVideoRecording() {
        binding.buttonToggleCamera.isEnabled = true
        binding.buttonCaptureImage.isEnabled = true
        binding.buttonRecordVideo.setText(R.string.start_record_video)
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
