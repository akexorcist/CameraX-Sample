package com.akexorcist.example.camerax.helper

import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

open class ShortenMultiplePermissionListener : MultiplePermissionsListener {
    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
    }

    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken?) {
        token?.continuePermissionRequest()
    }
}