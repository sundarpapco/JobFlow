package com.sivakasi.papco.jobflow.data

// A data class used to track the history of a job completion
data class ProcessingHistory(
    val destinationId:String="",
    val destinationName:String="",
    val completionTime:Long=0L
)
