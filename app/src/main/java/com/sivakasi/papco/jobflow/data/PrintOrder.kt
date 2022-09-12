package com.sivakasi.papco.jobflow.data

import android.content.Context
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.extensions.currentTimeInMillis
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import com.sivakasi.papco.jobflow.models.SearchModel

class PrintOrder {

    companion object {
        const val TYPE_NEW_JOB = -1
        const val TYPE_REPEAT_JOB = -2
        const val PO_NUMBER_NOT_YET_ALLOCATED = -3
        const val FIELD_PLATE_NUMBER = "plateMakingDetail.plateNumber"
        const val FIELD_PRINT_ORDER_NUMBER = "printOrderNumber"
        const val FIELD_CREATION_TIME = "creationTime"
        const val FIELD_INVOICE_NUMBER = "invoiceDetails"
        const val FIELD_COMPLETED_TIME = "completionTime"
        const val FIELD_CLIENT_ID = "clientId"

        fun documentId(poNumber: Int): String {
            require(poNumber > 0) { "Invalid print order number while generating document ID" }
            return "po$poNumber"
        }
    }

    var creationTime: Long = currentTimeInMillis()
    var emergency: Boolean = false
    var jobType: Int = TYPE_NEW_JOB
    var printOrderNumber: Int = PO_NUMBER_NOT_YET_ALLOCATED
    var clientId: Int = -1
    var billingName: String = ""
    var jobName: String = ""
    var pendingRemarks: String = ""
    var paperDetails: MutableList<PaperDetail>? = null
    var plateMakingDetail: PlateMakingDetail = PlateMakingDetail()
    var printingDetail: PrintingDetail = PrintingDetail()
    var listPosition: Long = 0
    var invoiceDetails: String = ""
    var previousDestinationId: String = ""
    var completionTime: Long = 0
    var notes: String = ""

    //Stores the list of machine names which completes this job
    var processingHistory: List<ProcessingHistory> = emptyList()

    //Stores the list of partial dispatch invoice details if any
    var partialDispatches: List<PartialDispatch> = emptyList()

    var lamination: Lamination? = null
    var foil: String? = null
    var scoring: String? = null
    var folding: String? = null
    var binding: Binding? = null
    var spotUV: String? = null
    var aqueousCoating: String? = null
    var cutting: String? = null
    var packing: String? = null


    fun prepareForReprint() {
        creationTime = currentTimeInMillis()
        jobType = TYPE_REPEAT_JOB
        invoiceDetails = ""
        completionTime = 0L
        previousDestinationId = ""
        processingHistory = emptyList()
        partialDispatches = emptyList()
        if (clientId == -1)
            billingName = ""
    }


    fun ageString(): String {
        //86,400,000 is the number of milliseconds per day
        val calendar = getCalendarInstance()
        val now = calendar.timeInMillis
        val zoneOffset = calendar.timeZone.rawOffset

        val epochDaysSinceCreation = (creationTime + zoneOffset) / 86400000
        val epochDaysSinceNow = (now + zoneOffset) / 86400000

        return when (val differenceInDays = epochDaysSinceNow - epochDaysSinceCreation) {
            0L -> {
                "Today"
            }
            1L -> {
                "Yesterday"
            }
            else -> {
                "$differenceInDays days ago"
            }
        }
    }

    fun printingSizePaperDetail(): PaperDetail {

        val trimHeight = plateMakingDetail.trimmingHeight.toFloat() / 10f
        val trimWidth = plateMakingDetail.trimmingWidth.toFloat() / 10f
        val result = paperDetails?.fold(PaperDetail()) { acc, element ->
            acc.sheets =
                acc.sheets + (element.sheets * element.tilePaperSize(trimHeight, trimWidth))
            acc
        } ?: error("No paper details found in print order")


        result.height = trimHeight
        result.width = trimWidth
        if (paperDetails!!.size > 1) {
            result.gsm = 0
            result.name = ""
        } else {
            result.gsm = paperDetails!![0].gsm
            result.name = paperDetails!![0].name
        }

        return result
    }

    fun documentId(): String =
        documentId(printOrderNumber)

}

fun PrintOrder.toSearchModel(context: Context, destinationId: String): SearchModel {

    val result = SearchModel(context)
    val printOrder = this
    with(result) {
        printOrderNumber = printOrder.printOrderNumber
        creationTime = printOrder.creationTime
        printOrderDate = calendarWithTime(printOrder.creationTime).asDateString()
        billingName = printOrder.billingName
        jobName = printOrder.jobName
        plateNumber = printOrder.plateMakingDetail.plateNumber
        invoiceNumber = printOrder.invoiceDetails
        colors = printOrder.printingDetail.colours
        this.destinationId = destinationId
        paperDetails = printOrder.printingSizePaperDetail().toString()
        completionTime = printOrder.completionTime
        dispatchCount = printOrder.partialDispatches.size
    }
    return result
}

fun PrintOrder.completeProcessingHistory(context: Context): List<ProcessingHistory> {

    val result = mutableListOf<ProcessingHistory>()

    val creation = ProcessingHistory(
        destinationId = context.getString(R.string.po_created),
        destinationName = context.getString(R.string.po_created),
        completionTime = creationTime
    )

    result.add(creation)
    processingHistory.forEach {
        result.add(
            ProcessingHistory(
                destinationId = it.destinationId,
                destinationName = it.destinationName,
                completionTime = it.completionTime
            )
        )
    }

    if (result.last().destinationId != DatabaseContract.DOCUMENT_DEST_COMPLETED &&
        invoiceDetails.isNotBlank()
    )
        result.add(
            ProcessingHistory(
                destinationId = DatabaseContract.DOCUMENT_DEST_COMPLETED,
                destinationName = DatabaseContract.DOCUMENT_DEST_COMPLETED,
                completionTime = completionTime
            )
        )

    return result
}