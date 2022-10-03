package com.sivakasi.papco.jobflow.screens.search

import android.content.Context
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.models.SearchModel
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AlgoliaRecord(
    val printOrderNumber: Int,
    val billingName: String,
    val jobName: String,
    val creationTime: Long,
    val plateNumber: Int,
    val paperDetail: String,
    val invoiceDetails: String,
    val colours: String,
    val printingInstructions: String,
    val destinationId: String,
    val partialDispatches: List<String> = emptyList()
)

/*
Converts the result from the algolia to SearchModel.
If the SearchQuery parameter is provided, it is used to check if the user is searching the partial
dispatch Invoice number. If the query contains any of the partial dispatch invoice number, then the
invoice number in this SearchModel is replaced with the partial dispatch Invoice number so that the user
can see the partial invoice number in the search result instead of the final invoice number

 */
fun AlgoliaRecord.toSearchModel(context: Context, searchQuery: String = ""): SearchModel {
    val result = SearchModel(context)
    result.printOrderNumber = printOrderNumber
    result.billingName = billingName
    result.jobName = jobName
    result.printOrderDate = calendarWithTime(creationTime).asDateString()
    result.plateNumber = plateNumber
    result.paperDetails = paperDetail
    result.invoiceNumber = invoiceDetails
    result.colors = colours
    result.creationTime = creationTime
    result.destinationId = destinationId
    result.dispatchCount = partialDispatches.size

    if (partialDispatches.isNotEmpty() && searchQuery.isNotEmpty()) {
        val query = searchQuery.lowercase(Locale.getDefault())
        partialDispatches.forEach {
            if (query.contains(it.lowercase(Locale.getDefault())))
                result.invoiceNumber = it
        }
    }

    return result
}

