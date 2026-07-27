package com.sivakasi.papco.jobflow.preview.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.nav3.util.ToasterState
import com.sivakasi.papco.jobflow.preview.JobPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ViewPreviewScreenState : ToasterState() {

    var isWaiting by mutableStateOf(false)
    var preview:JobPreview? by mutableStateOf(null)

}