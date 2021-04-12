package com.sivakasi.papco.jobflow.extensions

import android.util.Log
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/*Helper function which will get the number from the editText and will return the given value
if the editText is blank. Will throw exception if the editText contains non numberic characters
 */

fun EditText.isBlank():Boolean=text.toString().isBlank()
fun EditText.isNotBlank():Boolean=!isBlank()

fun EditText.number(valueIfBlank: Int = -1): Int {

    return text.toString().let {
        if (it.isBlank())
            valueIfBlank
        else
            it.toInt()
    }

}

fun EditText.numberDecimal(valueIfBlank:Float = -1f): Float {

    return text.toString().let {
        if (it.isBlank())
            valueIfBlank
        else
            it.toFloat()
    }

}

fun TextInputEditText.validateForGreaterThanZero(errorMsg: String): Boolean {

    val value = this.numberDecimal()
    return if (value <= 0) {
        (this.parent.parent as TextInputLayout).error = errorMsg
        false
    } else
        true

}

fun TextInputEditText.validateForNonBlank(errorMsg: String): Boolean {

    return if (this.text.toString().isBlank()) {
        (this.parent.parent as TextInputLayout).error = errorMsg
        false
    } else
        true
}


/*
Function to convert string like 58.3 -> 58.3
but 58.0 -> 58 (Instead of 58.0)
 */
fun Float.asString(): String {
    val asInt = this.toInt()
    return if (this - asInt == 0f)
        asInt.toString()
    else
        this.toString()
}


fun log(msg: String) {
    Log.d("SUNDAR", msg)
}

