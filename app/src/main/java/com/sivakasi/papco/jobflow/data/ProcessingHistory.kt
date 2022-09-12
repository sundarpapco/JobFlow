package com.sivakasi.papco.jobflow.data

import com.sivakasi.papco.jobflow.extensions.asReadableTimeStamp
import com.sivakasi.papco.jobflow.extensions.calendarWithTime

// A data class used to track the history of a job completion
data class ProcessingHistory(
    val destinationId:String="",
    val destinationName:String="",
    val completionTime:Long=0L
){
    val timeStamp:String = calendarWithTime(completionTime).asReadableTimeStamp()

}
