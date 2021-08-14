package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.Counter
import com.sivakasi.papco.jobflow.data.DatabaseContract

class CreateClientTransaction(
    private val client: Client
): Transaction.Function<Boolean>  {

    private val database = FirebaseFirestore.getInstance()

    private val counterRef = database.collection(DatabaseContract.COLLECTION_COUNTERS)
        .document(DatabaseContract.DOCUMENT_COUNTER_CLIENT_ID)


    //Initial value Documents
    private var clientCounter=Counter()


    override fun apply(transaction: Transaction): Boolean {

        val clientCounterDocument=transaction.get(counterRef)

        if(clientCounterDocument.exists())
            clientCounter=clientCounterDocument.toObject(Counter::class.java)!!

        //Make changes and prepare the things to write
        clientCounter.value++
        client.id=clientCounter.value
        val clientRef=database.collection(DatabaseContract.COLLECTION_CLIENTS)
            .document(client.documentId())

        transaction.set(counterRef,clientCounter)
        transaction.set(clientRef,client)
        return true
    }
}