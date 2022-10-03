package com.sivakasi.papco.jobflow.extensions

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.toSearchModel
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

fun DocumentSnapshot.toSearchModel(context: Context,searchQuery:String=""): SearchModel {
    val printOrder = toObject(PrintOrder::class.java)!!
    return printOrder.toSearchModel(
        context,
        reference.parent.parent?.id ?: error("Invalid Job path"),
        searchQuery
    )
}