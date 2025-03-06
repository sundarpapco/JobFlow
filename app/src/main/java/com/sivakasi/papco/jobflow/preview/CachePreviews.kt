package com.sivakasi.papco.jobflow.preview

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class CachePreviews(private val context: Context) {

    private val previewId = "temp" + getCalendarInstance().timeInMillis.toString()
    var previews: List<JobPreview> by mutableStateOf(emptyList())

    suspend fun cacheAndAddPreview(fileUri: Uri) {

        val preview = JobPreview(
            context = context,
            previewId = previewId
        )

        val cachedFile = preview.cacheContentFromUri(fileUri)
        previews = previews + preview.copy(
            displayUrl = Uri.fromFile(cachedFile).toString()
        )
    }

    fun removePreview(preview: JobPreview): Boolean {

        val previewToDelete = previews.find {
            it==preview
        } ?: return false

        try {
           preview.deleteCachedFile()
        } catch (_: Exception) { }

        previews = previews - previewToDelete
        return true

    }

    fun clearAllPreviews(){

        if(previews.isEmpty())
            return

        try{

            for(preview in previews){
                preview.deleteCachedFile()
            }

            val dirToDelete=previews[0].cacheDirectory()
            if(dirToDelete.exists())
                dirToDelete.delete()

        }catch(_:Exception){}
    }

    fun previewsToUpload(previewId:String):List<JobPreview> {

        val previewsToUpload = mutableListOf<JobPreview>()

        if(previews.isEmpty())
            return previewsToUpload

        for(preview in previews){
            previewsToUpload.add(preview.copy(previewId=previewId))
        }

        val tempDirectory = previews[0].cacheDirectory()
        val previewDirectory = previewsToUpload[0].cacheDirectory()

        val renamingSuccess=tempDirectory.renameTo(previewDirectory)
        if(!renamingSuccess)
            throw IllegalStateException("Cannot rename the temp directory")

        return previewsToUpload

    }

}