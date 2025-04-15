package com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.extensions.floatNumber
import com.sivakasi.papco.jobflow.extensions.intNumber
import com.sivakasi.papco.jobflow.screens.manageprintorder.SheetsExpressionChecker

class PaperDetailDialogState(
    private val context: Context,
    val editingIndex:Int,
    paperDetail: PaperDetail
) {

    var isPartyPaper by mutableStateOf(paperDetail.partyPaper)
    var height by mutableStateOf(
        if (paperDetail.height == 0f)
            TextFieldValue("")
        else
            TextFieldValue(paperDetail.height.toString())
    )
    var heightError:String? by mutableStateOf(null)
    var width by mutableStateOf(
        if (paperDetail.width == 0f)
            TextFieldValue("")
        else
            TextFieldValue(paperDetail.width.toString())
    )
    var widthError:String? by mutableStateOf(null)

    var gsm by mutableStateOf(
        if (paperDetail.gsm == 0)
            TextFieldValue("")
        else
            TextFieldValue(paperDetail.gsm.toString())
    )
    var gsmError:String? by mutableStateOf(null)

    var substrate by mutableStateOf(TextFieldValue(paperDetail.name))
    var substrateError:String? by mutableStateOf(null)
    var expression by mutableStateOf(TextFieldValue(""))
    var sheets by mutableStateOf(
        if (paperDetail.sheets == 0)
            TextFieldValue("")
        else
            TextFieldValue(paperDetail.sheets.toString())
    )
    var sheetsError:String? by mutableStateOf(null)


    fun validate():Boolean{
        var valid=true

        if(height.text.floatNumber(0f)==0f){
            heightError=context.getString(R.string.required_field)
            valid=false
        }


        if(width.text.floatNumber(0f)==0f) {
            widthError = context.getString(R.string.required_field)
            valid=false
        }

        if(gsm.text.intNumber(0)==0) {
            gsmError = context.getString(R.string.required_field)
            valid=false
        }

        if(substrate.text.isBlank()) {
            substrateError = context.getString(R.string.required_field)
            valid=false
        }

        if(sheets.text.intNumber(0)==0){
            valid=false
            sheetsError=context.getString(R.string.required_field)
        }

        return valid

    }

    fun evaluateExpression(){

        val enteredExpression = expression.text.trim()
        if (enteredExpression.isNotBlank()) {
            /*
            Try to evaluate the expression.
            Surrounding inside the try..catch block to handle exception cases like user purposely
            entered a very very long number string inside the expression field which will throw
            Numeric exception like calculated total sheets exceeds Int or even long
             */
            try {
                val expressionEvaluator = SheetsExpressionChecker(enteredExpression)
                if (expressionEvaluator.isValid) {
                    val sheetsString=expressionEvaluator.totalSheets().toString()
                    sheets = TextFieldValue(
                        text = sheetsString,
                        selection = TextRange(0,sheetsString.length)
                    )
                }else{
                    sheets = TextFieldValue(
                        text = "0",
                        selection = TextRange(0,1)
                    )
                }
            } catch (e: Exception) {
                sheets = TextFieldValue(
                    text = "0",
                    selection = TextRange(0,1)
                )
            }
        }

    }


    fun toPaperDetail():PaperDetail{
        return PaperDetail(
            partyPaper = isPartyPaper,
            height = height.text.floatNumber(0f),
            width = width.text.floatNumber(0f),
            gsm= gsm.text.intNumber(0),
            name = substrate.text.trim(),
            sheets = sheets.text.intNumber(0)
        )
    }
}