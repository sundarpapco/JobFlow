package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.*
import com.sivakasi.papco.jobflow.extensions.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.toDestination
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import java.util.*

class MovePrintOrdersTransaction(
    private val sourceDocumentId: String,
    private val destinationDocumentId: String,
    private val movingJobs: List<PrintOrderUIModel>,
    private inline val apply: (PrintOrder) -> Unit
) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val movingPrintOrders: MutableList<PrintOrder> = LinkedList()

    private val sourceDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(sourceDocumentId)

    private val destinationDocumentRef =
        database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(destinationDocumentId)

    //Temp variables to read and process all the moving jobs
    private lateinit var tempDocumentRef: DocumentReference
    private lateinit var tempDocumentSnapshot: DocumentSnapshot
    private lateinit var tempPrintOrder: PrintOrder

    private lateinit var source: Destination
    private var destination: Destination = Destination().apply {
        name = destinationDocumentId
        type = Destination.TYPE_FIXED
        timeBased = false
    }

    override fun apply(transaction: Transaction): Boolean {

        //Get and init the source and destination documents
        var totalMovingJobsDuration = 0
        val sourceDocumentSnapshot = transaction.get(sourceDocumentRef)
        val destinationDocumentSnapshot = transaction.get(destinationDocumentRef)
        source = if (sourceDocumentSnapshot.exists())
            sourceDocumentSnapshot.toDestination()
        else
            error("Source document not found")

        if (destinationDocumentSnapshot.exists())
            destination = destinationDocumentSnapshot.toDestination()


        var listPosition = currentTimeInMillis()

        /*
        The size of the movingJobs collection List is not always the actual number of moving jobs.
        In some extreme conditions, like retrying this transaction for many times due to connectivity issues,
        it is possible that the jobs in the collection might not exist due to various reasons. The following
        loop checks that condition and ignores those Jobs and proceed ahead with the remaining jobs.
        So, movingJobs.size != actualMovingJobsCount
        The following variable will keep track of it
         */
        var actualMovingJobsCount=0


        //Fetch and ready all the jobs which are moving
        for (job in movingJobs) {

            //Fetch
            tempDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(job.documentId())
            tempDocumentSnapshot = transaction.get(tempDocumentRef)

            //If some jobs were not found while trying to move, just ignore that job and move
            //ahead with other jobs
            if (!tempDocumentSnapshot.exists())
                continue
            else
                actualMovingJobsCount++

            //Make ready
            tempPrintOrder = tempDocumentSnapshot.toObject(PrintOrder::class.java)!!
            totalMovingJobsDuration += tempPrintOrder.printingDetail.runningMinutes
            tempPrintOrder.listPosition = listPosition
            tempPrintOrder.previousDestinationId = sourceDocumentId
            apply(tempPrintOrder)
            listPosition++

            /*
            Since we have added Clients as a late feature, this check is made to make sure no print order
            from now on can be moved without having a valid client ID
             */
            if (tempPrintOrder.clientId < 0)
                throw FirebaseFirestoreException(
                    "Invalid client ID found in ${tempPrintOrder.documentId()}",
                    FirebaseFirestoreException.Code.ABORTED
                )

            movingPrintOrders.add(tempPrintOrder)
        }

        //Make ready of source and destination documents
        source.lastJobCompletion = currentTimeInMillis()
        source.jobCount -= actualMovingJobsCount
        source.runningTime -= totalMovingJobsDuration

        destination.jobCount += movingPrintOrders.size
        destination.runningTime += totalMovingJobsDuration


        // Write all the values to the database
        //write the source and destination
        transaction.set(sourceDocumentRef, source)
        transaction.set(destinationDocumentRef, destination)

        //Delete jobs from source
        for (printOrder in movingPrintOrders) {
            tempDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(printOrder.documentId())

            transaction.delete(tempDocumentRef)
        }

        //Write all the jobs in the new destination
        for (printOrder in movingPrintOrders) {
            tempDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(destinationDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(printOrder.documentId())

            transaction.set(tempDocumentRef, printOrder)
        }

        //returning false because we don't need to refresh the adapter after this operation as
        //DiffUtil will take of it automatically since the jobs have been moved destinations
        return false
    }
}