package com.akexorcist.example.camerax.ui

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.akexorcist.example.camerax.R
import com.akexorcist.example.camerax.helper.ShortenMultiplePermissionListener
import com.akexorcist.example.camerax.helper.ShortenSeekBarChangeListener
import com.akexorcist.example.camerax.helper.applyWindowInserts
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import kotlinx.android.synthetic.main.activity_advance_image_capture.*
import java.io.File


class AdvanceImageCaptureActivity : AppCompatActivity() {
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var isTorchModeEnabled = false

    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance_image_capture)

        viewSafeArea.applyWindowInserts()
        buttonCaptureImage.setOnClickListener { captureImage() }
        buttonToggleCamera.setOnClickListener { switchCamera() }
        buttonToggleFlash.setOnClickListener { changeFlashMode() }
        buttonToggleTorch.setOnClickListener { changeTorchMode() }
        previewView.setOnTouchListener(onPreviewTouchListener)
        seekBarZoom.setOnSeekBarChangeListener(onSeekBarChangeListener)

        requestRuntimePermission()
        orientationEventListener.enable()
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationEventListener.disable()
    }

    private fun requestRuntimePermission() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.CAMERA)
            .withListener(multiplePermissionsListener)
            .check()
    }

    private fun setupCameraProvider() {
        ProcessCameraProvider.getInstance(this).also { provider ->
            provider.addListener(Runnable {
                cameraProvider = provider.get()
                bindCamera()
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindCamera() {
        val preview: Preview = createPreview()
        val cameraSelector = createCameraSelector()
        imageCapture = createCameraCapture()
        camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        camera?.let { camera ->
            preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
            setupCameraSetting(camera)
        }
    }

    private fun setupCameraSetting(camera: Camera) {
        camera.cameraInfo.zoomState.observe(this, zoomStateObserver)
        changeZoomLevel(0f)
        updateFlashAvailable(camera.cameraInfo.hasFlashUnit())
        updateTorchAvailable(camera.cameraInfo.hasFlashUnit())
    }

    private fun updateFlashAvailable(isEnabled: Boolean) {
        buttonToggleFlash.isEnabled = isEnabled
        updateFlashModeButton()
    }

    private fun updateTorchAvailable(isEnabled: Boolean) {
        buttonToggleTorch.isEnabled = isEnabled
        updateTorchModeButton()
    }

    private fun updateImageCaptureRotation(orientation: Int) {
        imageCapture?.targetRotation = when (orientation) {
            in 45..134 -> Surface.ROTATION_270
            in 135..224 -> Surface.ROTATION_180
            in 225..314 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
    }

    private fun unbindCamera() {
        camera?.cameraInfo?.zoomState?.removeObserver(zoomStateObserver)
        cameraProvider?.unbindAll()
    }

    private fun createPreview(): Preview = Preview.Builder()
        .build()

    private fun createCameraCapture(): ImageCapture = ImageCapture.Builder()
        .setFlashMode(flashMode)
        .setTargetResolution(Size(1200, 1200))
        .build()

    private fun createCameraSelector(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    private fun switchCamera() {
        lensFacing = when (lensFacing) {
            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
            else -> CameraSelector.LENS_FACING_BACK
        }
        unbindCamera()
        bindCamera()
    }

    private fun changeFlashMode() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            imageCapture?.flashMode = flashMode
            updateFlashModeButton()
        }
    }

    private fun changeZoomLevel(@FloatRange(from = 0.0, to = 1.0) level: Float) {
        camera?.cameraControl?.setLinearZoom(level)
    }

    private fun changeTorchMode() {
        isTorchModeEnabled = !isTorchModeEnabled
        updateFlashAvailable(!isTorchModeEnabled)
        camera?.cameraControl?.enableTorch(isTorchModeEnabled)
        updateTorchModeButton()

    }

    private fun captureImage() {
        val file = File(filesDir.absoluteFile, "temp.jpg")
        val outputFileOptions: ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), imageSavedCallback)
    }

    private fun performAutoFocus(x: Float, y: Float) {
        camera?.let { camera ->
            val pointFactory: MeteringPointFactory = previewView.createMeteringPointFactory(createCameraSelector())
            val afPointWidth = 1.0f / 6.0f
            val aePointWidth = afPointWidth * 1.5f
            val afPoint = pointFactory.createPoint(x, y, afPointWidth)
            val aePoint = pointFactory.createPoint(x, y, aePointWidth)
            val future = camera.cameraControl.startFocusAndMetering(
                FocusMeteringAction.Builder(
                    afPoint,
                    FocusMeteringAction.FLAG_AF
                ).addPoint(
                    aePoint,
                    FocusMeteringAction.FLAG_AE
                ).build()
            )
            future.addListener(Runnable {}, ContextCompat.getMainExecutor(this))
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

    private val orientationEventListener: OrientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                updateImageCaptureRotation(orientation)
            }
        }
    }

    private val onPreviewTouchListener = object : View.OnTouchListener {
        private var actionDownTimestamp: Long = 0
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                actionDownTimestamp = System.currentTimeMillis()
            } else if (event.action == MotionEvent.ACTION_UP && isInLongPressDuration()) {
                view.performClick()
                performAutoFocus(event.x, event.y)
            }
            return true
        }

        private fun isInLongPressDuration(): Boolean =
            System.currentTimeMillis() - actionDownTimestamp < ViewConfiguration.getLongPressTimeout()
    }

    private val onSeekBarChangeListener = object : ShortenSeekBarChangeListener() {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            changeZoomLevel(progress / 100f)
        }
    }

    private val zoomStateObserver = Observer { state: ZoomState ->
        textViewZoomLevel.text = getString(R.string.zoom_ratio, state.zoomRatio)
    }

    private val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            showResultMessage(getString(R.string.image_capture_success))
        }

        override fun onError(exception: ImageCaptureException) {
            showResultMessage(getString(R.string.image_capture_error, exception.message, exception.imageCaptureError))
        }
    }

    private fun onPermissionGrant() {
        setupCameraProvider()
    }

    private fun onPermissionDenied() {
        showResultMessage(getString(R.string.permission_denied))
        finish()
    }

    private fun updateFlashModeButton() {
        val mode = getString(
            when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> R.string.flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.string.flash_auto
                else -> R.string.flash_off
            }
        )
        val drawable = getDrawable(
            when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
        buttonToggleFlash.text = mode
        buttonToggleFlash.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    private fun updateTorchModeButton() {
        val mode = getString(
            when (isTorchModeEnabled) {
                true -> R.string.torch_on
                false -> R.string.torch_off
            }
        )
        val drawable = getDrawable(
            when (isTorchModeEnabled) {
                true -> R.drawable.ic_torch_on
                false -> R.drawable.ic_torch_off
            }
        )
        buttonToggleTorch.text = mode
        buttonToggleTorch.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
