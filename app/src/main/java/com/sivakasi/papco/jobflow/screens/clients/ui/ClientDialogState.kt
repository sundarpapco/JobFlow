package com.sivakasi.papco.jobflow.screens.clients.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ClientDialogState(
    val title:String,
    val positiveButtonText:String="SAVE",
    val negativeButtonText:String="CANCEL"
) {
    var id:Int=-1
    var name by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var error:String? by mutableStateOf(null)
}