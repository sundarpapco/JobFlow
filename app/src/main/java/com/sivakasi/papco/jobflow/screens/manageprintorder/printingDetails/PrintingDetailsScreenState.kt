package com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintingDetail

class PrintingDetailsScreenState(
    private val context:Context,
    printingDetails: PrintingDetail,
    val isCorrectionAllowed:Boolean
) {
    var colors by mutableStateOf(
        TextFieldValue(
            text = printingDetails.colours,
            selection = TextRange(printingDetails.colours.length)
        )
    )
    var correction by mutableStateOf(
        TextFieldValue(
            text = printingDetails.correction,
            selection = TextRange(printingDetails.correction.length)
        )
    )
    var printingInstructions by mutableStateOf(printingDetails.printingInstructions)
    var runningMinutes by mutableIntStateOf(printingDetails.runningMinutes)
    var hasSpotColours:Boolean = false

    var coloursError: String? by mutableStateOf(null)
    var runningTimeError:String? by mutableStateOf(null)

    var isRunningTimeDialogShowing by mutableStateOf( false)

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

fun PrintingDetailsScreenState.toPrintingDetails():PrintingDetail{

    return PrintingDetail(
        colours = this.colors.text.trim(),
        printingInstructions = this.printingInstructions.trim(),
        correction = if(isCorrectionAllowed)this.correction.text.trim() else "",
        runningMinutes = this.runningMinutes,
        hasSpotColours = this.hasSpotColours
    )

}