package com.akexorcist.example.camerax.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.core.content.ContextCompat
import com.akexorcist.example.camerax.R
import com.akexorcist.example.camerax.helper.ShortenMultiplePermissionListener
import com.akexorcist.example.camerax.helper.ShortenSeekBarChangeListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import kotlinx.android.synthetic.main.activity_camera_view.*
import java.io.File

class CameraViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_view)
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .withListener(multiplePermissionsListener)
            .check()
    }

    private fun setupView() {
        buttonToggleCamera.setOnClickListener { onToggleCameraClick() }
        buttonCaptureImage.setOnClickListener { onCaptureImageClick() }
        buttonRecordVideo.setOnClickListener { onRecordVideoClick() }
        seekBarZoom.setOnSeekBarChangeListener(seekBarChangeListener)
        seekBarZoom.max = ((cameraView.maxZoomRatio - cameraView.minZoomRatio) * 10).toInt()
        seekBarZoom.progress = (cameraView.zoomRatio * 10).toInt()

        cameraView.bindToLifecycle(this)
        Log.e("Check", "Max Zoom : ${cameraView.maxZoomRatio}")
        Log.e("Check", "Min Zoom : ${cameraView.minZoomRatio}")
        Log.e("Check", "Max : ${seekBarZoom.max}")
        Log.e("Check", "Progress : ${seekBarZoom.progress}")
    }

    private fun onToggleCameraClick() {
        cameraView.toggleCamera()
    }

    private fun onCaptureImageClick() {
        val file = File(filesDir.absoluteFile, "temp.jpg")
        cameraView.takePicture(file, ContextCompat.getMainExecutor(this), imageSavedCallback)
    }

    private fun onRecordVideoClick() {
        if (cameraView.isRecording) {
            cameraView.stopRecording()
            onStopVideoRecording()
        } else {
            startVideoRecording()
            val file = File(filesDir.absoluteFile, "temp.mp4")
            cameraView.startRecording(file, ContextCompat.getMainExecutor(this), videoSavedCallback)
        }
    }

    private fun onPermissionGrant() {
        setupView()
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

    private val videoSavedCallback = object : VideoCapture.OnVideoSavedCallback {
        override fun onVideoSaved(file: File) {
            showResultMessage(getString(R.string.video_record_success))
        }

        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
            showResultMessage(getString(R.string.video_record_error, message, videoCaptureError))
        }
    }

    private val seekBarChangeListener = object : ShortenSeekBarChangeListener() {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            cameraView.zoomRatio = progress / 10f
        }
    }

    private fun startVideoRecording() {
        buttonToggleCamera.isEnabled = false
        buttonCaptureImage.isEnabled = false
        buttonRecordVideo.setText(R.string.stop_record_video)
    }

    private fun onStopVideoRecording() {
        buttonToggleCamera.isEnabled = true
        buttonCaptureImage.isEnabled = true
        buttonRecordVideo.setText(R.string.start_record_video)
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
