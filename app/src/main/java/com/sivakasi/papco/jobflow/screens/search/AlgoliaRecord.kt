package com.sivakasi.papco.jobflow.screens.search

import android.content.Context
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.models.SearchModel
import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaRecord(
    val printOrderNumber:Int,
    val billingName:String,
    val jobName:String,
    val creationTime:Long,
    val plateNumber:Int,
    val paperDetail:String,
    val invoiceDetails:String,
    val colours:String,
    val printingInstructions:String,
    val destinationId:String,
    val partialDispatches:List<String> = emptyList()
)

fun AlgoliaRecord.toSearchModel(context:Context):SearchModel{
    val result=SearchModel(context)
    result.printOrderNumber = printOrderNumber
    result.billingName = billingName
    result.jobName=jobName
    result.printOrderDate= calendarWithTime(creationTime).asDateString()
    result.plateNumber=plateNumber
    result.paperDetails=paperDetail
    result.invoiceNumber=invoiceDetails
    result.colors=colours
    result.creationTime=creationTime
    result.destinationId=destinationId
    result.dispatchCount=partialDispatches.size

    return result
}

