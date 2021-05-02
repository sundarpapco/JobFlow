package com.sivakasi.papco.jobflow.data

import com.sivakasi.papco.jobflow.currentTimeInMillis
import com.sivakasi.papco.jobflow.getCalendarInstance

class PrintOrder {

    companion object {
        const val TYPE_NEW_JOB = -1
        const val TYPE_REPEAT_JOB = -2
        const val PO_NUMBER_NOT_YET_ALLOCATED = -3
    }

    var creationTime: Long = currentTimeInMillis()
    var emergency: Boolean = false
    var jobType: Int = TYPE_NEW_JOB
    var printOrderNumber: Int = PO_NUMBER_NOT_YET_ALLOCATED
    var billingName: String = ""
    var jobName: String = ""
    var pendingRemarks: String = ""
    var paperDetails: MutableList<PaperDetail>? = null
    var plateMakingDetail: PlateMakingDetail = PlateMakingDetail()
    var printingDetail: PrintingDetail = PrintingDetail()
    var listPosition: Long = 0
    var invoiceDetails:String=""
    var previousDestinationId:String=""
    var completionTime:Long=0
    var notes:String=""

    var lamination: Lamination? = null
    var foil: String? = null
    var scoring: String? = null
    var folding: String? = null
    var binding: Binding? = null
    var spotUV: String? = null
    var aqueousCoating: String? = null
    var cutting: String? = null
    var packing: String? = null


    fun prepareForReprint(){
        creationTime= currentTimeInMillis()
        jobType=TYPE_REPEAT_JOB
        invoiceDetails=""
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
        if(paperDetails!!.size > 1){
            result.gsm=0
            result.name=""
        }else{
            result.gsm = paperDetails!![0].gsm
            result.name = paperDetails!![0].name
        }

        return result
    }

    fun documentId(): String {

        require(printOrderNumber > 0) { "Invalid print order number while generating document ID" }
        return "po$printOrderNumber"
    }

}