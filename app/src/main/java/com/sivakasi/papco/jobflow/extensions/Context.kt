package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.sivakasi.papco.jobflow.R
import java.io.File

fun Context.shareReport(localFilePath: String) {

    val fileToShare= File(localFilePath)
    val path: Uri = FileProvider.getUriForFile(
        this,
        getString(R.string.file_sharing_authority),
        fileToShare
    )
    val sharingIntent = Intent(Intent.ACTION_SEND)

    sharingIntent.type = "application/*"
    sharingIntent.putExtra(Intent.EXTRA_STREAM, path)
    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Report from Papco Payroll")
    startActivity(Intent.createChooser(sharingIntent, "Share Report via..."))

}