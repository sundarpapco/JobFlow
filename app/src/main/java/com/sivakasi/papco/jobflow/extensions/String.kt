package com.sivakasi.papco.jobflow.extensions

import androidx.core.text.isDigitsOnly
import java.util.regex.Pattern

fun String.intNumber(valueIfBlank:Int):Int{
    return if(isBlank())
        valueIfBlank
    else
        this.toInt()
}

fun String.floatNumber(valueIfBlank: Float):Float{
    return if(isBlank())
        valueIfBlank
    else
        this.toFloat()
}

fun String.isPositiveFloatNumber():Boolean{
    val floatExpression="^(?:[1-9]\\d*|0)?(?:\\.\\d*)?$"
    return Pattern.matches(floatExpression,this)
}

fun String.isPositiveNumber():Boolean{
    return this.isDigitsOnly()
}

