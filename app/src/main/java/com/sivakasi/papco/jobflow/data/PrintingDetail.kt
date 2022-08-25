package com.sivakasi.papco.jobflow.data

data class PrintingDetail(
    var colours:String="4",
    var printingInstructions:String="",
    var runningMinutes:Int=0,
    var hasSpotColours:Boolean=false
)
