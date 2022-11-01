package com.sivakasi.papco.jobflow.preview

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.extensions.getAllPendingWorksForPreviewId
import com.sivakasi.papco.jobflow.extensions.toJobPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class PreviewManagementVM @Inject constructor(
    private val application: Application,
    private val repository: Repository
) : ViewModel() {

    private var isAlreadyObserving = false
    val screenState = PreviewManagementScreenState()

    fun uploadFileToStorage(fileUri: Uri) {

        viewModelScope.launch {

            try {
                val jobPreview = JobPreview(
                    application,
                    screenState.previewId ?: error("previewId in screen state is null")
                )
                jobPreview.cacheAndUploadFile(fileUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun observePreviews(previewId: String) {

        if (isAlreadyObserving)
            return
        else
            isAlreadyObserving = true

        screenState.previewId = previewId
        viewModelScope.launch(Dispatchers.IO) {

            repository.observePreviews(previewId)
                .combine(workersFlow(previewId)) { serverList, localList ->
                    var combinedList = serverList + localList
                    combinedList = combinedList.distinctBy { "${it.previewId}${it.fileName}" }
                    combinedList.sortedBy { it.fileName }
                }
                .collect {
                    screenState.imageUris = it
                }
        }
    }

    fun deletePreview(preview: JobPreview) {
        screenState.hideDeleteConfirmationDialog()
        viewModelScope.launch {
            repository.deletePreview(preview)
        }
    }

    private fun workersFlow(previewId: String): Flow<List<JobPreview>> {
        return application.getAllPendingWorksForPreviewId(previewId)
            .asFlow()
            .map { workInfos ->
                workInfos.map {
                    it.toJobPreview(application, previewId)
                }
            }
    }
}