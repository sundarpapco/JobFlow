package com.sivakasi.papco.jobflow.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.sivakasi.papco.jobflow.transactions.CreatePrintOrderTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
class Repository {

    private val database=FirebaseFirestore.getInstance()


    suspend fun createPrintOrder(printOrder:PrintOrder)= suspendCancellableCoroutine<Boolean> {continuation->

        database.runTransaction(CreatePrintOrderTransaction(printOrder))
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }

    }


    suspend fun searchPrintOrderByPlateNumber(plateNumber:Int):PrintOrder?= suspendCancellableCoroutine{continuation->

        database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
            .whereEqualTo("plateMakingDetail.plateNumber",plateNumber)
            .orderBy("creationTime",Query.Direction.DESCENDING).limit(1)
            .get(Source.SERVER)
            .addOnSuccessListener {
                if(it.documents.isEmpty())
                    continuation.resume(null)
                else {
                    val result = it.documents[0].toObject(PrintOrder::class.java)
                    continuation.resume(result)
                }
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }

    }

}