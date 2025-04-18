package com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder

class JobDetailsScreenState(
    private val context: Context
) {

    var isEditMode:Boolean=false
        private set
    var clientId:Int=0
        private set
    var editingCompletedPO by mutableStateOf(false)
    var clientName by mutableStateOf("")
    var clientNameError:String? by mutableStateOf(null)

    var jobName by mutableStateOf(TextFieldValue(""))
    var jobNameError:String? by mutableStateOf(null)

    var pendingRemarks by mutableStateOf("")
    var invoiceDetail by mutableStateOf("")
    var isUrgent by mutableStateOf(false)

    fun selectClient(id:Int, name:String){
        clientId=id
        clientName=name
    }

    fun loadPrintOrder(printOrder: PrintOrder,parentDestinationId:String,editMode:Boolean){
        isEditMode=editMode
        editingCompletedPO = parentDestinationId==DatabaseContract.DOCUMENT_DEST_COMPLETED
        clientId=printOrder.clientId
        clientName=printOrder.billingName
        jobName=TextFieldValue(
            printOrder.jobName,
            selection = TextRange(printOrder.jobName.length,printOrder.jobName.length)
        )
        pendingRemarks=printOrder.pendingRemarks
        invoiceDetail=printOrder.invoiceDetails
        isUrgent=printOrder.emergency
    }

    fun saveToPrintOrder(printOrder: PrintOrder){
        printOrder.clientId=clientId
        printOrder.billingName=clientName.trim()
        printOrder.jobName=jobName.text.trim()
        printOrder.pendingRemarks=pendingRemarks.trim()
        printOrder.invoiceDetails=invoiceDetail.trim()
        printOrder.emergency=isUrgent
    }

    fun validate():Boolean{

        var valid=true

        if(clientName.isBlank()){
            valid=false
            clientNameError=context.getString(R.string.required_field)
        }

        if(jobName.text.isBlank()){
            valid=false
            jobNameError=context.getString(R.string.required_field)
        }

        return valid
    }

}