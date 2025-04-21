package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.ui.TextInputDialogState
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class PostPressScreenState(
    private val context: Context
) {

    private val loadingChannel = Channel<LoadingStatus>()
    val loadingStatus=loadingChannel.receiveAsFlow()

    var isWaiting by mutableStateOf(false)

    var isEditMode by mutableStateOf(false)
        private set

    var bindingDialogState: BindingDialogState? by mutableStateOf(null)
        private set

    var remarksDialogState: TextInputDialogState<String>? by mutableStateOf(null)
        private set

    var laminationDialogState:LaminationDialogState? by mutableStateOf(null)
    private set


    var lamination: Lamination? by mutableStateOf(null)
    var foil: String? by mutableStateOf(null)
    var scoring: String? by mutableStateOf(null)
    var folding: String? by mutableStateOf(null)
    var binding: Binding? by mutableStateOf(null)
    var spotUV: String? by mutableStateOf(null)
    var aqueousCoating: String? by mutableStateOf(null)
    var cutting: String? by mutableStateOf(null)
    var packing: String? by mutableStateOf(null)

    suspend fun showLoadingStatus(){
        loadingChannel.send( LoadingStatus.Loading(context.getString(R.string.one_moment_please)))
    }

    suspend fun loadingErrorStatus(e:Exception){
        loadingChannel.send(LoadingStatus.Error(e))
    }

    suspend fun <T>loadingSuccessStatus(data:T){
        loadingChannel.send(LoadingStatus.Success(data))
    }

    fun showLaminationDialog() {
        laminationDialogState = LaminationDialogState()
    }

    fun dismissLaminationDialog() {
        if(laminationDialogState != null)
            laminationDialogState = null
    }

    fun showBindingDialog() {
        bindingDialogState = BindingDialogState()
    }

    fun dismissBindingDialog() {
           bindingDialogState=null
    }

    fun showRemarksDialog(
        dialogCode: String,
        title: String,
        defaultValue: String = ""
    ) {
        remarksDialogState = TextInputDialogState<String>(
            positiveButtonText = context.getString(R.string.save),
            negativeButtonText = context.getString(R.string.cancel)
        ).apply {
            this.title = title
            data = dialogCode
            text = TextFieldValue(defaultValue)
            label = context.getString(R.string.remarks)
        }
    }

    fun hideRemarksDialog() {
        if (remarksDialogState != null) {
            remarksDialogState = null
        }
    }

    fun loadPrintOrder(printOrder: PrintOrder,editMode:Boolean) {
        isEditMode=editMode
        lamination = printOrder.lamination
        foil = printOrder.foil
        scoring = printOrder.scoring
        folding = printOrder.folding
        binding = printOrder.binding
        spotUV = printOrder.spotUV
        aqueousCoating = printOrder.aqueousCoating
        cutting = printOrder.cutting
        packing = printOrder.packing
    }

    fun saveToPrintOrder(printOrder: PrintOrder) {
        printOrder.lamination = lamination
        printOrder.foil = foil
        printOrder.scoring = scoring
        printOrder.folding = folding
        printOrder.binding = binding
        printOrder.spotUV = spotUV
        printOrder.aqueousCoating = aqueousCoating
        printOrder.cutting = cutting
        printOrder.packing = packing
    }

}