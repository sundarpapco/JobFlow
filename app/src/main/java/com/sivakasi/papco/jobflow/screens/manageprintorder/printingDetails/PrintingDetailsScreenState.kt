package com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.PrintingDetail

class PrintingDetailsScreenState(
    private val context:Context
) {

    var isEditMode:Boolean=false
        private set
    var isCorrectionAllowed by mutableStateOf(false)
    var colors by mutableStateOf(
        TextFieldValue("")
    )
    var correction by mutableStateOf(
        TextFieldValue(""))

    var printingInstructions by mutableStateOf("")
    var runningMinutes by mutableIntStateOf(0)
    var hasSpotColours:Boolean = false

    var coloursError: String? by mutableStateOf(null)
    var runningTimeError:String? by mutableStateOf(null)
    var isRunningTimeDialogShowing by mutableStateOf( false)


    fun loadPrintOrder(printOrder:PrintOrder,editMode:Boolean){

        isEditMode=editMode
        val printingDetails = printOrder.printingDetail
        colors = TextFieldValue(
            text = printingDetails.colours,
            selection = TextRange(printingDetails.colours.length)
        )

        correction = TextFieldValue(
            text = printingDetails.correction,
            selection = TextRange(printingDetails.correction.length)
        )

        printingInstructions = printingDetails.printingInstructions
        runningMinutes = printingDetails.runningMinutes
        hasSpotColours = printingDetails.hasSpotColours
        isCorrectionAllowed = printOrder.jobType==PrintOrder.TYPE_REPEAT_JOB
    }

    fun saveToPrintOrder(printOrder: PrintOrder){
        val printingDetail = PrintingDetail(
            colours = this.colors.text.trim(),
            printingInstructions = this.printingInstructions.trim(),
            correction = if(isCorrectionAllowed)this.correction.text.trim() else "",
            runningMinutes = this.runningMinutes,
            hasSpotColours = this.hasSpotColours
        )
        printOrder.printingDetail = printingDetail
    }

    fun validate():Boolean{

        var isValid =true
        if(colors.text.isEmpty()){
            coloursError=context.getString(R.string.required_field)
            isValid=false
        }

        if(runningMinutes<=0){
            runningTimeError=context.getString(R.string.error_invalid_minutes)
            isValid=false
        }

        return isValid
    }
}