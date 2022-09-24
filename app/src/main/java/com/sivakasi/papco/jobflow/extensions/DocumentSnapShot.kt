package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.models.SearchModel

fun DocumentSnapshot.toPrintOrder(): PrintOrder =
    toObject(PrintOrder::class.java)!!

fun DocumentSnapshot.toPrintOrderUIModel(): PrintOrderUIModel =
    PrintOrderUIModel.fromPrintOrder(toPrintOrder())


fun DocumentSnapshot.toDestination():Destination {
    return toObject(Destination::class.java)!!.also {
        it.id = this.id
    }
}

fun DocumentSnapshot.toSearchModel(context: Context): SearchModel {
    val result = SearchModel(context)
    val printOrder = toObject(PrintOrder::class.java)!!
    with(result) {
        printOrderNumber = printOrder.printOrderNumber
        creationTime=printOrder.creationTime
        printOrderDate = calendarWithTime(printOrder.creationTime).asDateString()
        billingName = printOrder.billingName
        jobName = printOrder.jobName
        plateNumber = printOrder.plateMakingDetail.plateNumber
        invoiceNumber = printOrder.invoiceDetails
        colors = printOrder.printingDetail.colours
        destinationId = reference.parent.parent?.id ?: error("Invalid Job path")
        paperDetails=printOrder.printingSizePaperDetail().toString()
        completionTime=printOrder.completionTime
    }
    return result
}