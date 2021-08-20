package com.sivakasi.papco.jobflow.models

import android.content.Context
import android.util.Log
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder

class SearchModel(private val context: Context) {

    var printOrderNumber: Int = PrintOrder.PO_NUMBER_NOT_YET_ALLOCATED
    var printOrderDate: String = ""
    var billingName: String = ""
    var jobName: String = ""
    var plateNumber: Int = PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED
    var paperDetails: String = ""
    var invoiceNumber: String = ""
    var colors: String = "4"
    var destinationId: String = ""
    var creationTime:Long=0L

    fun poNumberAndDate(): String =
        context.getString(R.string.po_number_and_date, printOrderNumber, printOrderDate)

    fun rid(): String =
        when (plateNumber) {
            PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE -> {
                context.getString(R.string.outside_plate)
            }

            PlateMakingDetail.PLATE_NUMBER_NOT_YET_ALLOCATED -> {
                error("Invalid plate number found while searching")
            }

            else -> {
                context.getString(R.string.rid_xx, plateNumber)
            }
        }


    fun status(): String {
        Log.d("SUNDAR", "destinationId: $destinationId")
        return when (destinationId) {

            DatabaseContract.DOCUMENT_DEST_COMPLETED -> {
                invoiceNumber
            }

            DatabaseContract.DOCUMENT_DEST_CANCELLED -> {
                context.getString(R.string.cancelled)
            }
            else -> {
                context.getString(R.string.active)
            }
        }
    }

}
