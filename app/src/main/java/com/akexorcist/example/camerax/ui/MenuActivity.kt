package com.akexorcist.example.camerax.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.akexorcist.example.camerax.R
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        buttonBuiltInCameraView.setOnClickListener {
            startActivity(Intent(this, CameraViewActivity::class.java))
        }

        buttonImageAnalyzer.setOnClickListener {
            startActivity(Intent(this, LuminosityAnalyzerActivity::class.java))
        }

        buttonImageCapture.setOnClickListener {
            startActivity(Intent(this, ImageCaptureActivity::class.java))
        }
    }
}