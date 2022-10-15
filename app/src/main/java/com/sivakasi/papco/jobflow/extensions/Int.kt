package com.sivakasi.papco.jobflow.extensions

fun Int.asCommaSeparatedNumber():String{

    if (this == 0)
        return "0"

    val reversed = toString().reversed()
    var result=""

    for (i in 1..reversed.length) {
        result = if (i <= 3 || i % 2 != 0)
            reversed[i - 1] + result
        else
            reversed[i - 1] + "," + result
    }

    return result
}