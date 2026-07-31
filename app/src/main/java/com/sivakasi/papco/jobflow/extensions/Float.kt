package com.sivakasi.papco.jobflow.extensions

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
