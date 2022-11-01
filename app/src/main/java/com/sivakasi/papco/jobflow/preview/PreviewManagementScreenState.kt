package com.sivakasi.papco.jobflow.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PreviewManagementScreenState {

    var previewId:String?=null
    var imageUris: List<JobPreview> by mutableStateOf(emptyList())
    var previewToDelete:JobPreview? by mutableStateOf(null)

    fun showDeleteConfirmation(preview:JobPreview){
        previewToDelete=preview
    }

    fun hideDeleteConfirmationDialog(){
        previewToDelete=null
    }
}