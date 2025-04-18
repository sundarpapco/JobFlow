package com.sivakasi.papco.jobflow.screens.manageprintorder.addJob

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AddJobScreenState {

    var isNewJob by mutableStateOf(true)
    var ridNumber by mutableStateOf("")
    var ridError:String? by mutableStateOf(null)
    var isWaiting by mutableStateOf(false)
        private set
    var isPONotFoundDialogShowing by mutableStateOf(false)
        private set

    fun showWaiting() {
        isWaiting = true
    }

    fun hideWaiting(){
        isWaiting=false
    }

    fun showIsPONotFoundDialog() {
        isWaiting = false
        isPONotFoundDialogShowing = true
    }

    fun hideIsPONotFoundDialog(){
        isPONotFoundDialogShowing=false
    }

}