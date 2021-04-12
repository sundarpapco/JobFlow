package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination

class DeleteMachineTransaction(machineId: String) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val documentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(machineId)
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var machine: Destination

    override fun apply(transaction: Transaction): Boolean? {

        //Check whether the machine exists
        documentSnapshot = transaction.get(documentRef)
        if (!documentSnapshot.exists())
            error("Machine not found")

        //If reaches here, then the machine exits. Check whether it doesn't have any jobs in it
        machine = documentSnapshot.toObject(Destination::class.java)!!
        if (machine.jobCount > 0)
            error("Cannot delete machine with jobs in it")


        //If it reaches here, then everything is fine for deletion. Go ahead and delete the document
        transaction.delete(documentRef)
        return true
    }
}