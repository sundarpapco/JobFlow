package com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder

class JobDetailsScreenState(
    private val context: Context,
    val isEditMode:Boolean
) {

    var clientName by mutableStateOf("")
    var clientNameError:String? by mutableStateOf(null)

    var jobName by mutableStateOf("")
    var jobNameError:String? by mutableStateOf(null)

    var pendingRemarks by mutableStateOf("")
    var invoiceDetail by mutableStateOf("")
    var isUrgent by mutableStateOf(false)

    fun loadPrintOrder(printOrder: PrintOrder){
        clientName=printOrder.billingName
        jobName=printOrder.jobName
        pendingRemarks=printOrder.pendingRemarks
        invoiceDetail=printOrder.invoiceDetails
        isUrgent=printOrder.emergency
    }

    fun validate():Boolean{

        var valid=true

        if(clientName.isBlank()){
            valid=false
            clientNameError=context.getString(R.string.required_field)
        }

        if(jobName.isBlank()){
            valid=false
            jobNameError=context.getString(R.string.required_field)
        }

        return valid
    }

}