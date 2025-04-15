package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.sivakasi.papco.jobflow.R
import java.io.File

fun Context.shareReport(localFilePath: String) {

    val fileToShare = File(localFilePath)
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

fun Context.sharePreview(previewImage: File) {

    val path: Uri = FileProvider.getUriForFile(
        this,
        getString(R.string.file_sharing_authority),
        previewImage
    )
    val sharingIntent = Intent(Intent.ACTION_SEND)

    sharingIntent.type = "image/jpeg"
    sharingIntent.putExtra(Intent.EXTRA_STREAM, path)
    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Preview")
    startActivity(Intent.createChooser(sharingIntent, "Share Preview via..."))

}

fun Context.toast(msg:String,duration:Int=Toast.LENGTH_SHORT){
    Toast.makeText(this, msg,duration).show()
}


fun Context.toastError(e: Exception) {
    val msg = e.getMessage(this)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toastStringResource(id: Int) {
    val msg = getString(id)
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.getAllPendingWorksForPreviewId(previewId: String): LiveData<List<WorkInfo>> {

    val workQuery = WorkQuery.Builder
        .fromUniqueWorkNames(listOf(previewId))
        .addStates(listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED))
        .build()

    return WorkManager.getInstance(this)
        .getWorkInfosLiveData(workQuery)
}

@Suppress("DEPRECATION")
fun Context.isNetConnected(): Boolean {

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}