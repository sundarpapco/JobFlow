package com.sivakasi.papco.jobflow.preview.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivakasi.papco.jobflow.preview.JobPreview
import com.sivakasi.papco.jobflow.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ViewPreviewVM : ViewModel() {

    val screenState = ViewPreviewScreenState()
    private val _sharePreview = MutableLiveData<Event<Any>>()
    val sharePreview: LiveData<Event<Any>> = _sharePreview


    fun loadPreview(preview: JobPreview) {
        screenState.preview = preview
    }

    fun sharePreview(preview: JobPreview) {

        screenState.isWaiting = true
        viewModelScope.launch {
            try {
                val file = preview.downloadFromServer()
                screenState.isWaiting = false
                _sharePreview.value = Event(file)
            } catch (e: Exception) {
                screenState.isWaiting = false
                _sharePreview.value = Event(e)
            }

        }

    }
}