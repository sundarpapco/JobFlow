package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import android.net.Uri
import androidx.work.WorkInfo
import com.sivakasi.papco.jobflow.preview.JobPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
fun WorkInfo.toJobPreview(context: Context, previewId:String):JobPreview{
    val tags=this.tags
    val filename=tags.filter { it.contains(".jpg")}
    require(filename.size ==1){"Error getting the filename from Tags"}
    val preview= JobPreview(
        context,
        previewId,
        filename[0]
    )
    return preview.copy(
        downloadUrl = Uri.fromFile(preview.localCacheFile()).toString()
    )
}