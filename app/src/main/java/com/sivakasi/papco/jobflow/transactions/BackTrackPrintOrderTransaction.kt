package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.extensions.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.destinationReference
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import java.util.*

class BackTrackPrintOrderTransaction(
    private val sourceDocumentId: String,
    private val movingJobs: List<PrintOrderUIModel>
) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val destinationsToUpdate: MutableList<Destination> = LinkedList()

    //Return value is a boolean which indicates whether we should refresh the recyclerview adapter
    //after this operation. We need to refresh if some jobs in the selection are unaffected by this
    //transaction.
    override fun apply(transaction: Transaction): Boolean {

        var totalRunningTime: Int
        var listPosition: Long = currentTimeInMillis()
        var destination: Destination

        val source = readSourceDestination(transaction)
        val groupedJobs = readAllJobs(transaction).groupBy { it.previousDestinationId }

        //Modify values of all read documents and prepare for writing
        for (destinationId in groupedJobs.keys) {

            destination = readDestination(transaction, destinationId)
            val jobs = groupedJobs[destinationId]!!
            totalRunningTime = 0

            for (job in jobs) {
                job.listPosition = listPosition
                job.previousDestinationId = sourceDocumentId

                totalRunningTime += job.printingDetail.runningMinutes
                listPosition++
            }

            destination.jobCount += jobs.size
            destination.runningTime += totalRunningTime
            destinationsToUpdate.add(destination)

            source.jobCount -= jobs.size
            source.runningTime -= totalRunningTime
        }

        //Write everything to database

        for (entry in groupedJobs) {
            moveJobs(transaction, entry.key, entry.value)
        }
        writeDestinations(transaction, destinationsToUpdate)
        writeSourceDestination(transaction, source)

        //returning false because we don't need to refresh the adapter after this operation as
        //DiffUtil will take of it automatically since the jobs have been moved destinations
        return false
    }

    private fun readSourceDestination(
        transaction: Transaction,
    ): Destination {

        val reference = database.destinationReference(sourceDocumentId)

        val snapshot = transaction.get(reference)
        require(snapshot.exists()) { "Source not found" }
        return snapshot.toObject(Destination::class.java)!!.apply {
            id = sourceDocumentId
        }

    }

    private fun readDestination(transaction: Transaction, destinationId: String): Destination {

        val destinationDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(destinationId)
        val destinationDocumentSnapshot = transaction.get(destinationDocumentRef)

        return if (!destinationDocumentSnapshot.exists())
            Destination().apply {
                id = destinationId
                name = destinationId
                type = Destination.TYPE_FIXED
                timeBased = true
            }
        else
            destinationDocumentSnapshot.toObject(Destination::class.java)!!.apply {
                id = destinationDocumentSnapshot.id
            }
    }

    private fun readAllJobs(transaction: Transaction): List<PrintOrder> {

        var reference: DocumentReference
        var snapShot: DocumentSnapshot
        val result = LinkedList<PrintOrder>()

        for (job in movingJobs) {
            reference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(job.documentId())

            snapShot = transaction.get(reference)
            if (snapShot.exists())
                result.add(snapShot.toObject(PrintOrder::class.java)!!)

        }

        return result
    }

    private fun writeDestinations(transaction: Transaction, destinations: List<Destination>) {

        var reference: DocumentReference
        for (destination in destinations) {
            reference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(destination.id)
            transaction.set(reference, destination)
        }
    }

    private fun moveJobs(transaction: Transaction, destinationId: String, jobs: List<PrintOrder>) {

        var reference: DocumentReference
        for (job in jobs) {
            reference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(destinationId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(job.documentId())
            transaction.set(reference, job)

            reference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(job.documentId())
            transaction.delete(reference)
        }

    }

    private fun writeSourceDestination(transaction: Transaction, destination: Destination) {

        val reference = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(sourceDocumentId)

        destination.id = sourceDocumentId
        transaction.set(reference, destination)
    }


}