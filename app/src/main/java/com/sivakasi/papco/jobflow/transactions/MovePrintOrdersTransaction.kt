package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.*
import com.sivakasi.papco.jobflow.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import java.util.*

class MovePrintOrdersTransaction(
    private val sourceDocumentId: String,
    private val destinationDocumentId: String,
    private val movingJobs: List<PrintOrderUIModel>,
    private val apply:(PrintOrder)->Unit
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
    private lateinit var tempPrintOrder:PrintOrder

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
            sourceDocumentSnapshot.toObject(Destination::class.java)!!
        else
            error("Source document not found")

        if (destinationDocumentSnapshot.exists())
            destination = destinationDocumentSnapshot.toObject(Destination::class.java)!!


        var listPosition= currentTimeInMillis()
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

            //Make ready
            tempPrintOrder=tempDocumentSnapshot.toObject(PrintOrder::class.java)!!
            totalMovingJobsDuration+=tempPrintOrder.printingDetail.runningMinutes
            tempPrintOrder.listPosition=listPosition
            tempPrintOrder.previousDestinationId=sourceDocumentId
            //tempPrintOrder.invoiceDetails=invoiceDetail
            apply(tempPrintOrder)
            listPosition++
            movingPrintOrders.add(tempPrintOrder)
        }

        //Make ready of source and destination documents
        source.lastJobCompletion= currentTimeInMillis()
        source.jobCount -= movingPrintOrders.size
        source.runningTime -= totalMovingJobsDuration

        destination.jobCount += movingPrintOrders.size
        destination.runningTime += totalMovingJobsDuration


        // Write all the values to the database
        //write the source and destination
        transaction.set(sourceDocumentRef,source)
        transaction.set(destinationDocumentRef,destination)

        //Delete jobs from source
        for(printOrder in movingPrintOrders){
            tempDocumentRef=database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(printOrder.documentId())

            transaction.delete(tempDocumentRef)
        }

        //Write all the jobs in the new destination
        for(printOrder in movingPrintOrders){
            tempDocumentRef=database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(destinationDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(printOrder.documentId())

            transaction.set(tempDocumentRef,printOrder)
        }

        return true
    }
}