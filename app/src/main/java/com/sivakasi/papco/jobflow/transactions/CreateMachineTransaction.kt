package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.extensions.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination

class CreateMachineTransaction(
    private val machineName: String
) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val documentReference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document()
    private lateinit var documentSnapShot: DocumentSnapshot
    private var machine = Destination().apply {
        name = machineName
        type = Destination.TYPE_DYNAMIC
        timeBased = true
        creationTime= currentTimeInMillis()
    }

    override fun apply(transaction: Transaction): Boolean? {

        //First try to read the machine document and lets see if it already exists
        documentSnapShot = transaction.get(documentReference)
        if (documentSnapShot.exists())
            throw FirebaseFirestoreException(
                "Machine with the given name already exists",
                FirebaseFirestoreException.Code.ALREADY_EXISTS
            )

        transaction.set(documentReference, machine)
        return true
    }
}