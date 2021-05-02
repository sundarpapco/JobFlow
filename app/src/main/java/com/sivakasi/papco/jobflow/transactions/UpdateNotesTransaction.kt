package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException

class UpdateNotesTransaction(
    destinationId:String,
    poId:String,
    private val newNotes:String
):Transaction.Function<Boolean> {

    private val database=FirebaseFirestore.getInstance()
    private val documentReference=database.poReference(destinationId,poId)

    override fun apply(transaction: Transaction): Boolean {

        //Get the document first
        val documentSnapshot = transaction.get(documentReference)
        if(!documentSnapshot.exists())
            throw ResourceNotFoundException("")

        val printOrder=documentSnapshot.toObject(PrintOrder::class.java)!!
        printOrder.notes=newNotes

        transaction.set(documentReference,printOrder)
        return true

    }
}