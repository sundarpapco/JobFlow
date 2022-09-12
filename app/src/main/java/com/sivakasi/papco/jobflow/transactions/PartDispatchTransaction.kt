package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PartialDispatch
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import java.util.*

class PartDispatchTransaction(
    private val sourceDocumentId: String,
    private val dispatchingJobs: List<PrintOrderUIModel>,
    invoiceDetail:String
) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()
    private val dispatchingPrintOrders: MutableList<PrintOrder> = LinkedList()
    private val partialDispatch = PartialDispatch(
        invoiceDetail,
        getCalendarInstance().timeInMillis
    )

    //Temp variables to read and process all the moving jobs
    private lateinit var tempDocumentRef: DocumentReference
    private lateinit var tempDocumentSnapshot: DocumentSnapshot
    private lateinit var tempPrintOrder: PrintOrder


    override fun apply(transaction: Transaction): Boolean {


        //Fetch and ready all the jobs which are moving
        for (job in dispatchingJobs) {

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
            tempPrintOrder = tempDocumentSnapshot.toObject(PrintOrder::class.java)!!
            tempPrintOrder.partialDispatches = tempPrintOrder.partialDispatches + partialDispatch
            dispatchingPrintOrders.add(tempPrintOrder)
        }

        // Write all the values to the database
        for (printOrder in dispatchingPrintOrders) {
            tempDocumentRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(sourceDocumentId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(printOrder.documentId())

            transaction.set(tempDocumentRef, printOrder)
        }

        //returning false because we don't need to refresh the adapter after this operation as
        //DiffUtil will take of it automatically since the jobs have been moved destinations
        return true
    }
}