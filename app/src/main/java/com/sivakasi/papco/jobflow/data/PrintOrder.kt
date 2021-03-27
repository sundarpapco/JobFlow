package com.sivakasi.papco.jobflow.data

import com.sivakasi.papco.jobflow.currentTimeInMillis

class PrintOrder {

    companion object {
        const val TYPE_NEW_JOB = -1
        const val TYPE_REPEAT_JOB = -2
        const val PO_NUMBER_NOT_YET_ALLOCATED = -3
    }

    var creationTime: Long = currentTimeInMillis()
    var jobType: Int = TYPE_NEW_JOB
    var printOrderNumber: Int = PO_NUMBER_NOT_YET_ALLOCATED
    var billingName: String = ""
    var jobName: String = ""
    var paperDetails: MutableList<PaperDetail>? = null
    var plateMakingDetail: PlateMakingDetail = PlateMakingDetail()
    var printingDetail: PrintingDetail = PrintingDetail()

    var lamination: Lamination? = null
    var foil: String? = null
    var scoring: String? = null
    var folding: String? = null
    var binding: Binding? = null
    var spotUV: String? = null
    var aqueousCoating: String? = null
    var cutting: String? = null
    var packing: String? = null

    fun documentId(): String {

        require(printOrderNumber > 0){"Invalid print order number while generating document ID"}
        return "po$printOrderNumber"
    }

}