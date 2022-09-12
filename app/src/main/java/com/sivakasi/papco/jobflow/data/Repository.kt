package com.sivakasi.papco.jobflow.data

import android.app.Application
import androidx.core.text.isDigitsOnly
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.google.firebase.firestore.*
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.extensions.toPrintOrder
import com.sivakasi.papco.jobflow.extensions.toPrintOrderUIModel
import com.sivakasi.papco.jobflow.extensions.toSearchModel
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.transactions.*
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
@ViewModelScoped
class Repository @Inject constructor(
    private val application: Application
) {

    private val database = FirebaseFirestore.getInstance()

    fun observeUser(
        userId: String,
    ) = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null)
                    throw firebaseFirestoreException

                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    try {
                        trySend(null).isSuccess
                    } catch (e: Exception) {
                    }
                } else
                    try {
                        trySend(documentSnapshot.toObject(User::class.java)).isSuccess
                    } catch (e: Exception) {
                    }
            }

        awaitClose { listenerRegistration.remove() }
    }


    suspend fun observeDestination(
        destinationId: String,
    ) = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(destinationId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null)
                    throw firebaseFirestoreException

                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    try {
                        trySend(null).isSuccess
                    } catch (e: Exception) {
                    }
                } else
                    try {
                        val destination = documentSnapshot.toObject(Destination::class.java)
                        destination?.id=documentSnapshot.id
                        trySend(destination).isSuccess
                    } catch (e: Exception) {
                    }
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun observePrintOrder(destinationId: String, poId: String) = callbackFlow {

        val listenerRegistration = database.poReference(destinationId, poId)
            .addSnapshotListener { documentSnapshot, firebaseFireStoreException ->

                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (documentSnapshot == null || !documentSnapshot.exists())
                    try {
                        trySend(null).isSuccess
                    } catch (e: Exception) {
                    }
                else
                    try {
                        trySend(documentSnapshot.toPrintOrder()).isSuccess
                    } catch (e: Exception) {

                    }
            }

        awaitClose { listenerRegistration.remove() }

    }

    suspend fun getLastCompletedPrintOrderWithPlateNumber(plateNumber: Int):PrintOrder?{

        return suspendCancellableCoroutine { continuation->
            database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(DatabaseContract.DOCUMENT_DEST_COMPLETED)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_PLATE_NUMBER,plateNumber)
                .orderBy(PrintOrder.FIELD_PRINT_ORDER_NUMBER,Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener{
                    if (it.documents.isEmpty())
                        continuation.resume(null)
                    else {
                        val result = it.documents[0].toPrintOrder()
                       continuation.resume(result)
                    }
                }
                .addOnFailureListener{
                    continuation.resumeWithException(it)
                }
        }

    }


    suspend fun getLatestPrintOrderWithPlateNumber(plateNumber: Int): PrintOrder? {

        if (!isValidReprintPlateNumber(plateNumber))
            throw IllegalArgumentException()

        return suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_PLATE_NUMBER, plateNumber)
                .orderBy(PrintOrder.FIELD_CREATION_TIME, Query.Direction.DESCENDING).limit(1)
                .get(Source.SERVER)
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
                        continuation.resume(null)
                    else {
                        val result = it.documents[0].toPrintOrder()
                        continuation.resume(result)
                    }
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }
    }


    suspend fun fetchPrintOrder(poNumber: Int): PrintOrder? =
        suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_PRINT_ORDER_NUMBER, poNumber)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
                        continuation.resume(null)
                    else {
                        val result = it.documents[0].toPrintOrder()
                        continuation.resume(result)
                    }
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }


    suspend fun invoiceHistory(loadParams: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, SearchModel> =
        suspendCancellableCoroutine { continuation ->

            var query = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(DatabaseContract.DOCUMENT_DEST_COMPLETED)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .orderBy(PrintOrder.FIELD_COMPLETED_TIME, Query.Direction.DESCENDING)

            loadParams.key?.let {
                query = query.startAfter(it)
            }

            query.limit(loadParams.loadSize.toLong())
                .get()
                .addOnSuccessListener {

                    val nextPageKey = if (it.documents.size < loadParams.loadSize) {
                        //There is no next page to load
                        null
                    } else {
                        //Store the last documentSnapshot as nextPageKey
                        it.documents.last()
                    }

                    val loadedList = if (it.documents.isEmpty()) {
                        //There is no next Page
                        emptyList()
                    } else {
                        val result = it.documents.map { doc -> doc.toSearchModel(application) }
                        result
                    }

                    continuation.resume(LoadResult.Page(loadedList, null, nextPageKey))
                }
                .addOnFailureListener {
                    continuation.resume(LoadResult.Error(it))
                }
        }

    private suspend fun searchByNumber(poNumber: Int): SearchModel? =
        suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_PRINT_ORDER_NUMBER, poNumber)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
                        continuation.resume(null)
                    else {
                        val result = it.documents[0].toSearchModel(application)
                        continuation.resume(result)
                    }
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }


    private suspend fun searchByRid(rid: Int): List<SearchModel> =
        suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_PLATE_NUMBER, rid)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
                        continuation.resume(emptyList())
                    else {
                        val result = it.documents.map { doc -> doc.toSearchModel(application) }
                        continuation.resume(result)
                    }
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }

    private suspend fun searchByInvoice(invoiceNumber: String): List<SearchModel> =
        suspendCancellableCoroutine { continuation ->

            database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_INVOICE_NUMBER, invoiceNumber)
                .get()
                .addOnSuccessListener {
                    if (it.documents.isEmpty())
                        continuation.resume(emptyList())
                    else {
                        val result = it.documents.map { doc -> doc.toSearchModel(application) }
                        continuation.resume(result)
                    }
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }

        }

    suspend fun clientHistory(
        clientId: Int,
        loadParams: LoadParams<DocumentSnapshot>
    ): LoadResult<DocumentSnapshot, SearchModel> =
        suspendCancellableCoroutine { continuation ->

            var query = database.collectionGroup(DatabaseContract.COLLECTION_JOBS)
                .whereEqualTo(PrintOrder.FIELD_CLIENT_ID, clientId)
                .orderBy(PrintOrder.FIELD_PRINT_ORDER_NUMBER, Query.Direction.DESCENDING)

            loadParams.key?.let {
                query = query.startAfter(it)
            }

            query.limit(loadParams.loadSize.toLong())
                .get()
                .addOnSuccessListener {
                    val nextPageKey = if (it.documents.size < loadParams.loadSize) {
                        //There is no next page to load
                        null
                    } else {
                        //Store the last documentSnapshot as nextPageKey
                        it.documents.last()
                    }

                    val loadedList = if (it.documents.isEmpty()) {
                        //There is no next Page
                        emptyList()
                    } else {
                        val result = it.documents.map { doc -> doc.toSearchModel(application) }
                        result
                    }

                    continuation.resume(LoadResult.Page(loadedList, null, nextPageKey))
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }


    suspend fun search(searchQuery: String): List<SearchModel> = withContext(Dispatchers.IO) {
        val searchByNumber = async {
            if (!searchQuery.isDigitsOnly())
                emptyList()
            else {
                val model = searchByNumber(searchQuery.toInt())
                model?.let {
                    listOf(it)
                } ?: emptyList()
            }
        }

        val searchByRid = async {
            if (!searchQuery.isDigitsOnly())
                emptyList()
            else
                searchByRid(searchQuery.toInt())
        }

        val searchByInvoice = async {
            searchByInvoice(searchQuery)
        }

        val result = LinkedList<SearchModel>()
        result.addAll(searchByRid.await())
        result.addAll(searchByInvoice.await())
        result.addAll(searchByNumber.await())
        result.sortByDescending { it.printOrderNumber }
        result.distinctBy { it.printOrderNumber }
    }


    fun jobsOfDestination(destinationId: String) = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
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
                        trySend(querySnapshot.documents).isSuccess
                    } catch (e: Exception) {

                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }.map {
        it.map { doc ->
            doc.toPrintOrderUIModel()
        }
    }.flowOn(Dispatchers.IO)

    fun getAllUsers() = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_USERS)
            .orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->
                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (querySnapshot == null)
                    throw ResourceNotFoundException("Users cannot be loaded")
                else {
                    try {
                        trySend(querySnapshot.documents).isSuccess
                    } catch (e: Exception) {

                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }.map {
        it.map { doc ->
            doc.toObject(User::class.java)!!
        }
    }


    suspend fun loadAllMachines() = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .whereEqualTo("type", Destination.TYPE_DYNAMIC)
            .orderBy("creationTime", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->

                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (querySnapshot == null)
                    throw ResourceNotFoundException("machines cannot be loaded")
                else
                    try {
                        trySend(querySnapshot.documents).isSuccess
                    } catch (e: Exception) {

                    }

            }
        awaitClose { listenerRegistration.remove() }

    }.map {
        it.map { document ->
            document.toObject(Destination::class.java)!!.apply {
                id = document.id
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun loadAllClients() = callbackFlow {

        val listenerRegistration = database.collection(DatabaseContract.COLLECTION_CLIENTS)
            .orderBy(Client.FIELD_NAME, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->

                if (firebaseFireStoreException != null)
                    throw firebaseFireStoreException

                if (querySnapshot == null)
                    throw ResourceNotFoundException("Clients cannot be loaded")
                else
                    trySend(querySnapshot.documents)


            }
        awaitClose { listenerRegistration.remove() }

    }.map {
        it.map { document ->
            document.toObject(Client::class.java)!!
        }
    }


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

    private suspend fun isValidReprintPlateNumber(plateNumber: Int): Boolean =
        suspendCancellableCoroutine { continuation ->
            database.collection(DatabaseContract.COLLECTION_COUNTERS)
                .document(DatabaseContract.DOCUMENT_COUNTER_RID)
                .get(Source.SERVER)
                .addOnSuccessListener {
                    val lastValue = it.toObject(Counter::class.java)!!.value
                    continuation.resume(plateNumber <= lastValue)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }


    suspend fun partDispatchJobs(
        sourceId: String,
        jobs: List<PrintOrderUIModel>,
        invoiceDetail:String
    ):Boolean =
        runTransaction(PartDispatchTransaction(sourceId,jobs,invoiceDetail))


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

    suspend fun updateNotes(parentDestinationId: String, poId: String, newNotes: String) =
        runTransaction(UpdateNotesTransaction(parentDestinationId, poId, newNotes))

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

    suspend fun clearPendingStatus(destinationId: String, jobs: List<PrintOrderUIModel>) =
        runTransaction(ClearPendingStatusTransaction(destinationId, jobs))

    suspend fun createClient(client: Client) =
        runTransaction(CreateClientTransaction(client))

    suspend fun updateClient(client: Client) =
        runTransaction(UpdateClientTransaction(client))


    suspend fun markAsPending(
        destinationId: String,
        remark: String,
        jobs: List<PrintOrderUIModel>
    ) =
        runTransaction(MarkAsPendingTransaction(destinationId, remark, jobs))


    private suspend fun runTransaction(transaction: Transaction.Function<Boolean>) =
        suspendCancellableCoroutine<Boolean> { continuation ->
            database.runTransaction(transaction)
                .addOnSuccessListener {
                    continuation.resume(it)
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