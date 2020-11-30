package com.akexorcist.example.camerax.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akexorcist.example.camerax.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private val binding: ActivityMenuBinding by lazy {
        ActivityMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonBuiltInCameraView.setOnClickListener {
            startActivity(Intent(this, CameraViewActivity::class.java))
        }

        binding.buttonImageAnalyzer.setOnClickListener {
            startActivity(Intent(this, LuminosityAnalyzerActivity::class.java))
        }

        binding.buttonImageCapture.setOnClickListener {
            startActivity(Intent(this, ImageCaptureActivity::class.java))
        }

        binding.buttonAdvanceImageCapture.setOnClickListener {
            startActivity(Intent(this, AdvanceImageCaptureActivity::class.java))
        }
    }
}