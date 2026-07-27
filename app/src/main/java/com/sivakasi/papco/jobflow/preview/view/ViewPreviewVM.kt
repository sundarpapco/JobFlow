package com.sivakasi.papco.jobflow.preview.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.preview.JobPreview
import com.sivakasi.papco.jobflow.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

@ExperimentalCoroutinesApi
class ViewPreviewVM : ViewModel() {

    val screenState = ViewPreviewScreenState()
    private val _sharePreview = Channel<File>()
    val sharePreview = _sharePreview.receiveAsFlow()


    fun loadPreview(preview: JobPreview) {

        if(screenState.preview!=null)
            return

        screenState.preview = preview
    }

    fun sharePreview(preview: JobPreview) {

        screenState.isWaiting = true
        viewModelScope.launch {
            try {
                val file = preview.downloadFromServer()
                screenState.isWaiting = false
                _sharePreview.send(file)
            } catch (e: Exception) {
                screenState.isWaiting = false
                screenState.toastError(e)
            }

        }

    }
}