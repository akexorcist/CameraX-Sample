package com.akexorcist.example.camerax

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class LuminosityAnalyzer(private val onLuminosityUpdated: ((Double) -> Unit)?) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()
        onLuminosityUpdated?.invoke(luma)
        image.close()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
