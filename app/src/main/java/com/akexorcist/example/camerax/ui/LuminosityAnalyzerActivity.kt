package com.akexorcist.example.camerax.ui

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.akexorcist.example.camerax.LuminosityAnalyzer
import com.akexorcist.example.camerax.R
import com.akexorcist.example.camerax.helper.ShortenMultiplePermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import kotlinx.android.synthetic.main.activity_luminosity_analyzer.*
import java.util.concurrent.Executors

class LuminosityAnalyzerActivity : AppCompatActivity() {
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_luminosity_analyzer)
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .withListener(multiplePermissionsListener).check()
    }

    private fun setupCameraProvider() {
        ProcessCameraProvider.getInstance(this).also { provider ->
            provider.addListener(Runnable {
                bindPreview(provider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(600, 600))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageAnalyzer)

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        camera?.let { camera ->
            preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
        }
    }

    private val imageAnalyzer = LuminosityAnalyzer { luma: Double ->
        runOnUiThread {
            textViewLuma.text = getString(R.string.luma_value, luma)
        }
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

    private fun onPermissionGrant() {
        setupCameraProvider()
    }

    private fun onPermissionDenied() {
        showResultMessage(getString(R.string.permission_denied))
        finish()
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
