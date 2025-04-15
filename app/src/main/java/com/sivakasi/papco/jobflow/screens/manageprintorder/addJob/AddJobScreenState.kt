package com.sivakasi.papco.jobflow.screens.manageprintorder.addJob

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.data.PrintOrder

class AddJobScreenState {

    var isNewJob by mutableStateOf(false)
    var ridNumber by mutableStateOf("")
    var isWaiting by mutableStateOf(false)
        private set
    var isPONotFoundDialogShowing by mutableStateOf(false)
        private set

    fun saveToPrintOrder(printOrder: PrintOrder) {
        if (isNewJob)
            printOrder.jobType = PrintOrder.TYPE_NEW_JOB
        else
            printOrder.jobType = PrintOrder.TYPE_REPEAT_JOB

    }

    fun showWaiting() {
        isWaiting = true
    }

    fun showIsPONotFoundDialog() {
        isWaiting = false
        isPONotFoundDialogShowing = true
    }

    fun hideIsPONotFoundDialog(){
        isPONotFoundDialogShowing=false
    }

}