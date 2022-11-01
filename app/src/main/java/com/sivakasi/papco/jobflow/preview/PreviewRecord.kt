package com.sivakasi.papco.jobflow.preview

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi

/* A Plain and Simple convenient data holder class to save and restore records from the fire store
 Database. Convert this class to JobPreview for extended functionality
 */

data class PreviewRecord(
    val previewId: String = "",
    val fileName: String = "",
    val downloadUrl: String = ""
) {
    val path: String
        get()="previews/$previewId/${fileName}"


}

@ExperimentalCoroutinesApi
fun PreviewRecord.toJobPreview(context: Context): JobPreview {
    return JobPreview(
        context,
        previewId,
        fileName,
        downloadUrl
    )
}