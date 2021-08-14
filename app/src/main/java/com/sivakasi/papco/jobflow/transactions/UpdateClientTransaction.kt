package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.DatabaseContract

class UpdateClientTransaction(
    private val updatingClient: Client
): Transaction.Function<Boolean>  {

    private val database = FirebaseFirestore.getInstance()

    private val documentRef=database.collection(DatabaseContract.COLLECTION_CLIENTS)
        .document(updatingClient.documentId())

    private lateinit var client:Client

    override fun apply(transaction: Transaction): Boolean {

        val clientDocument = transaction.get(documentRef)
        if(clientDocument.exists())
            client=clientDocument.toObject(Client::class.java)!!
        else
            throw FirebaseFirestoreException(
                "Old Client document not found",
                FirebaseFirestoreException.Code.NOT_FOUND
            )

        client.name=updatingClient.name
        transaction.set(documentRef,client)
        return true
    }

}