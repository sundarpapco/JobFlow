package com.sivakasi.papco.jobflow.data

import com.google.firebase.firestore.*
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.transactions.*
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
class Repository @Inject constructor() {

    private val database = FirebaseFirestore.getInstance()


    suspend fun observeDestination(
        destinationId: String,
    ) = callbackFlow {

        database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(destinationId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null)
                    throw firebaseFirestoreException

                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    try {
                        offer(null)
                    } catch (e: Exception) {

                    }
                }

                try {
                    offer(documentSnapshot!!.toObject(Destination::class.java))
                } catch (e: Exception) {

                }
            }

        awaitClose {}
    }

    suspend fun observePrintOrder(destinationId: String, poId: String) = callbackFlow {

        database.poReference(destinationId,poId)
            .addSnapshotListener { documentSnapshot, firebaseFireStoreException ->

                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    try {
                        offer(null)
                    } catch (e: Exception) {

                    }
                }

                try {
                    offer(documentSnapshot!!.toObject(PrintOrder::class.java))
                } catch (e: Exception) {

                }
            }

        awaitClose {  }

    }


    suspend fun searchPrintOrderByPlateNumber(plateNumber: Int): PrintOrder? {

        if(!isValidReprintPlateNumber(plateNumber))
            throw IllegalArgumentException()

        return suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo("plateMakingDetail.plateNumber", plateNumber)
                .orderBy("creationTime", Query.Direction.DESCENDING).limit(1)
                .get(Source.SERVER)
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
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


    suspend fun searchPrintOrder(poNumber: Int): PrintOrder? =
        suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo("printOrderNumber", poNumber)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
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

    fun jobsOfDestination(destinationId: String) = callbackFlow {

        database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(destinationId)
            .collection(DatabaseContract.COLLECTION_JOBS)
            .orderBy("listPosition", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->
                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (querySnapshot == null)
                    throw ResourceNotFoundException("Jobs cannot be loaded")
                else {
                    try {
                        offer(querySnapshot.documents)
                    } catch (e: Exception) {

                    }
                }
            }

        awaitClose { }
    }.map {
        it.map { doc ->
            val po = doc.toObject(PrintOrder::class.java)
            PrintOrderUIModel.fromPrintOrder(po ?: error("Null print order found"))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun loadAllMachines() = callbackFlow {

        database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .whereEqualTo("type", Destination.TYPE_DYNAMIC)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->

                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (querySnapshot == null)
                    throw ResourceNotFoundException("machines cannot be loaded")
                else
                    try {
                        offer(querySnapshot.documents)
                    } catch (e: Exception) {

                    }

            }
        awaitClose { }

    }.map {
        it.map { document ->
            document.toObject(Destination::class.java)!!.apply {
                id = document.id
            }
        }
    }.flowOn(Dispatchers.IO)


    suspend fun machineAlreadyExist(machineName: String): Boolean =
        suspendCancellableCoroutine { continuation ->

            database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .whereEqualTo("name", machineName)
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (querySnapShot.documents.size > 0)
                        continuation.resume(true)
                    else
                        continuation.resume(false)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }

    suspend fun isValidReprintPlateNumber(plateNumber: Int):Boolean= suspendCancellableCoroutine { continuation->
        database.collection(DatabaseContract.COLLECTION_COUNTERS)
            .document(DatabaseContract.DOCUMENT_COUNTER_RID)
            .get(Source.SERVER)
            .addOnSuccessListener {
                val lastValue=it.toObject(Counter::class.java)!!.value
                continuation.resume(plateNumber <= lastValue)
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }
    }

    suspend fun readPrintOrder(destinationId: String, poId: String) =
        suspendCancellableCoroutine<PrintOrder> { continuation ->
            database.poReference(destinationId, poId)
                .get()
                .addOnSuccessListener {
                    continuation.resume(it.toObject(PrintOrder::class.java)!!)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    suspend fun moveJobs(
        sourceId: String,
        destinationId: String,
        jobs: List<PrintOrderUIModel>,
        applyFunction: (PrintOrder) -> Unit = {}
    ): Boolean =
        runTransaction(MovePrintOrdersTransaction(sourceId, destinationId, jobs, applyFunction))

    suspend fun createPrintOrder(printOrder: PrintOrder) =
        runTransaction(CreatePrintOrderTransaction(printOrder))

    suspend fun updatePrintOrder(parentDestinationId: String, printOrder: PrintOrder) =
        runTransaction(UpdatePrintOrderTransaction(printOrder, parentDestinationId))

    suspend fun batchUpdateJobs(destinationId: String, jobList: List<PrintOrderUIModel>) =
        runBatch(UpdateJobsBatch(destinationId, jobList))

    suspend fun createMachine(machineName: String) =
        runTransaction(CreateMachineTransaction(machineName))

    suspend fun updateMachine(machineId: String, machineName: String) =
        runTransaction(UpdateMachineTransaction(machineId, machineName))

    suspend fun deleteMachine(machineId: String) =
        runTransaction(DeleteMachineTransaction(machineId))

    suspend fun backtrackJobs(sourceId: String, jobs: List<PrintOrderUIModel>) =
        runTransaction(BackTrackPrintOrderTransaction(sourceId, jobs))


    private suspend fun runTransaction(transaction: Transaction.Function<Boolean>) =
        suspendCancellableCoroutine<Boolean> { continuation ->
            database.runTransaction(transaction)
                .addOnSuccessListener {
                    continuation.resume(true)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    private suspend fun runBatch(batch: WriteBatch.Function) =
        suspendCancellableCoroutine<Unit> { continuation ->

            database.runBatch(batch)
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }
}