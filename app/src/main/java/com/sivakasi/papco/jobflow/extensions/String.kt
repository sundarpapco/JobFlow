package com.sivakasi.papco.jobflow.extensions

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