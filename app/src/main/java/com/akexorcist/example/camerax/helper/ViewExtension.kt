package com.akexorcist.example.camerax.helper

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

// Add extra spacing for translucent status bar and navigation bar
fun View.applyWindowInserts() {
    setOnApplyWindowInsetsListener { view, insets ->
        val topSize = insets.systemWindowInsetTop
        val bottomSize = insets.systemWindowInsetBottom
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.setMargins(params.leftMargin, params.topMargin + topSize, params.rightMargin, params.bottomMargin + bottomSize)
        view.layoutParams = params
        insets
    }
}
