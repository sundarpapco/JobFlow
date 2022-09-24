package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.extensions.toDestination

class UpdateMachineTransaction(
    machineId: String,
    private val machineName: String) :
    Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val documentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(machineId)
    private lateinit var documentSnapshot: DocumentSnapshot
    private lateinit var machine:Destination

    override fun apply(transaction: Transaction): Boolean {

        //Check whether the original document exist
        documentSnapshot = transaction.get(documentRef)
        if (!documentSnapshot.exists())
            throw FirebaseFirestoreException(
                "Editing machine not found",
                FirebaseFirestoreException.Code.NOT_FOUND
            )


        //Update the machine document
        machine=documentSnapshot.toDestination()
        machine.id=documentSnapshot.id
        machine.name=machineName

        //Write the updated machine document
        transaction.set(documentRef,machine)
        return true
    }
}