package com.sivakasi.papco.jobflow.transactions

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.destinationReference
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.extensions.toDestination

class UpdatePrintOrderTransaction(
    private val printOrder: PrintOrder,
    private val destinationId: String
) :
    Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()

    override fun apply(transaction: Transaction): Boolean {

        val parentDestination = readParentDestination(transaction,destinationId)
        val oldPrintOrder=readOldPrintOrder(transaction,printOrder.documentId(),destinationId)

        parentDestination.runningTime -= oldPrintOrder.printingDetail.runningMinutes
        parentDestination.runningTime += printOrder.printingDetail.runningMinutes
        //Some other person might have changed the list position. So, for safety copy the old position
        printOrder.listPosition=oldPrintOrder.listPosition

        updateParentDestination(transaction,parentDestination)
        updatePrintOrder(transaction,parentDestination.id,printOrder)

        return true

    }


    private fun readOldPrintOrder(
        transaction: Transaction,
        oldPoId: String,
        destinationId: String
    ): PrintOrder {

        val documentReference = database.poReference(destinationId, oldPoId)
        val documentSnapShot = transaction.get(documentReference)
        require(documentSnapShot.exists()) { "Original PO Not found" }
        return documentSnapShot.toObject(PrintOrder::class.java)!!

    }

    private fun readParentDestination(
        transaction: Transaction,
        destinationId: String
    ): Destination {

        val documentReference = database.destinationReference(destinationId)
        val documentSnapshot = transaction.get(documentReference)
        require(documentSnapshot.exists()) { "Parent destination not founf" }
        return documentSnapshot.toDestination()

    }

    private fun updateParentDestination(transaction: Transaction, destination: Destination) {

        val documentReference = database.destinationReference(destination.id)
        transaction.set(documentReference, destination)

    }

    private fun updatePrintOrder(
        transaction: Transaction,
        destinationId: String,
        printOrder: PrintOrder
    ) {

        val documentReference = database.poReference(destinationId, printOrder.documentId())
        transaction.set(documentReference, printOrder)

    }

}