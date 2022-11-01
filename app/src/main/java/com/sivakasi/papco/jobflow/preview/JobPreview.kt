package com.sivakasi.papco.jobflow.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import com.sivakasi.papco.jobflow.preview.workers.UploadPreviewWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

@ExperimentalCoroutinesApi
data class JobPreview(
    private val context:Context,
    val previewId: String,
    val fileName: String = generateFileName(),
    val downloadUrl: String = ""
) {
    companion object {
        const val FIELD_PREVIEW_ID = "previewId"

        fun generateFileName(): String {
            return getCalendarInstance().timeInMillis.toString() + ".jpg"
        }
    }

    val path: String
        get() = "previews/$previewId/${fileName}"


    fun localCacheFile(): File {

        require(previewId.isNotBlank() && fileName.isNotBlank()) {
            "Invalid previewId or filename while getting cache file"
        }

        val tempDirectory: File
        val cacheDirectory = context.cacheDir

        tempDirectory = File(cacheDirectory.absolutePath + "/previews/${previewId}")
        if (!tempDirectory.exists())
            tempDirectory.mkdirs()

        val filePath = tempDirectory.absolutePath + "/${fileName}"
        return File(filePath)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun cacheContentFromUri(
        fileUri: Uri
    ) = withContext(Dispatchers.IO) {

        //Step 1: Read the user selected file in to memory as Bitmap
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        //Step 2: Downscale the bitmap in to calculated downscale size
        val downScaledBitmap = if (isDownScaleNecessary(bitmap)) {
            val downscaledSize = downScaledSize(bitmap.width, bitmap.height)
            if (isLandscape(bitmap))
                Bitmap.createScaledBitmap(bitmap, downscaledSize.second, downscaledSize.first, true)
            else
                Bitmap.createScaledBitmap(bitmap, downscaledSize.first, downscaledSize.second, true)
        } else
            bitmap

        //Step 3: Save the downScaled Bitmap in to the cache file
        val fileToWrite = localCacheFile()
        val fos = FileOutputStream(fileToWrite)
        downScaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)

        //Step 4: Clean up stuff
        fos.close()
        inputStream?.close()
    }

    suspend fun cacheAndUploadFile(fileUri:Uri){
        cacheContentFromUri(fileUri)
        UploadPreviewWorker.startWith(context,this)
    }

    fun storageReference(): StorageReference {
        val storage = Firebase.storage.reference
        return storage.child(path)
    }

    suspend fun downloadUrl(): Uri =
        suspendCancellableCoroutine { continuation ->
            val reference = storageReference()
            reference.downloadUrl
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    /*
    Will return the document ID for this preview in the Fire store database.
    ID = {previewId}_{filename without extension}
     */
    fun documentId(): String {
        val filenameWithoutExtension = fileName.split(".")[0]
        require(filenameWithoutExtension.isNotBlank()) { "Filenames should not start with a ." }
        return "${previewId}_${filenameWithoutExtension}"
    }

    private fun downScaledSize(side1: Int, side2: Int): Pair<Int, Int> {

        val minEdge = min(side1, side2)
        val maxEdge = max(side1, side2)

        if (maxEdge <= 1600)
            return Pair(minEdge, maxEdge)

        val downScaleFactor = (160000.0 / maxEdge.toDouble()) / 100.0
        val requiredMinEdge = (minEdge * downScaleFactor).toInt()
        return Pair(requiredMinEdge, 1600)

    }

    private fun isLandscape(bitmap: Bitmap): Boolean {
        return bitmap.width >= bitmap.height
    }

    private fun isDownScaleNecessary(bitmap: Bitmap): Boolean =
        max(bitmap.width, bitmap.height) > 1600
}

@ExperimentalCoroutinesApi
fun JobPreview.toPreviewRecord():PreviewRecord{
    return PreviewRecord(previewId,fileName,downloadUrl)
}
