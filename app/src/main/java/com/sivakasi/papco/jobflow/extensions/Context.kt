package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.sivakasi.papco.jobflow.R
import java.io.File
import java.lang.Exception

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

fun Context.toastError(e:Exception){
    val msg = e.getMessage(this)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toastStringResource(id:Int){
    val msg= getString(id)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}