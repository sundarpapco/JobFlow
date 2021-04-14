package com.sivakasi.papco.jobflow.models

import androidx.recyclerview.widget.DiffUtil
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.util.Duration

data class PrintOrderUIModel(
    var printOrderNumber: Int = PrintOrder.PO_NUMBER_NOT_YET_ALLOCATED,
    var billingName: String = "",
    var jobName: String = "",
    var emergency: Boolean = false,
    var isReprint: Boolean = false,
    var listPosition: Long = 0,
    var poNumberAndAge: String = "",
    var printingSizePaperDetail: String = "",
    var runningTime: Duration=Duration(),
    var colors: String = "",
    var hasSpotColors:Boolean=false,
    var isPending:Boolean=false
) {
    companion object {

        fun fromPrintOrder(printOrder: PrintOrder): PrintOrderUIModel {
            return PrintOrderUIModel().apply {
                printOrderNumber = printOrder.printOrderNumber
                billingName = printOrder.billingName
                jobName = printOrder.jobName
                emergency = printOrder.emergency
                isReprint = printOrder.jobType == PrintOrder.TYPE_REPEAT_JOB
                listPosition = printOrder.listPosition
                poNumberAndAge = "${printOrder.printOrderNumber} - ${printOrder.ageString()}"
                printingSizePaperDetail = printOrder.printingSizePaperDetail().toString()
                runningTime = Duration.fromMinutes(printOrder.printingDetail.runningMinutes)
                colors = printOrder.printingDetail.colours
                isPending=printOrder.pendingRemarks.isNotBlank()
                hasSpotColors=printOrder.printingDetail.hasSpotColours
            }
        }
    }

    fun documentId():String{
        require(printOrderNumber > 0) { "Invalid print order number while generating document ID" }
        return "po$printOrderNumber"
    }
}

class PrintOrderUIModelDiff : DiffUtil.ItemCallback<PrintOrderUIModel>(){

    override fun areItemsTheSame(oldItem: PrintOrderUIModel, newItem: PrintOrderUIModel): Boolean {
        return oldItem.printOrderNumber==newItem.printOrderNumber
    }

    override fun areContentsTheSame(
        oldItem: PrintOrderUIModel,
        newItem: PrintOrderUIModel
    ): Boolean {
        return oldItem==newItem
    }
}