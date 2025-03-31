package com.sivakasi.papco.jobflow.data

data class PrintingDetail(
    var colours:String="4",
    var printingInstructions:String="",
    var runningMinutes:Int=0,
    var hasSpotColours:Boolean=false,
    // Correction - If the Job is reprint and we need to change a single plate or any other correction
    // In the same Plate Number. If there is any correction needed, then it wont be an empty string
    // Note:Print Order will have a separate color for Reprint with Correction Jobs
    var correction:String=""
)
