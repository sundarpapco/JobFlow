package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.extensions.toPrintOrder
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import java.util.*

class MarkAsPendingTransaction(
    private val destinationId: String,
    private val pendingRemark: String,
    private val jobs: List<PrintOrderUIModel>
) : Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()

    //Will be set to true if some of the jobs we process doesn't have pending remarks in the beginning itself
    //We need to return true if there are mixed jobs to the UI because if mixed Jobs, then we need the
    //adapter to refresh. Else we need to return false
    private var mixedJobs = false

    //Return value is a boolean which indicates whether we should refresh the recyclerview adapter
    //after this operation. We need to refresh if some jobs in the selection are unaffected by this
    //transaction.
    override fun apply(transaction: Transaction): Boolean {

        val jobsToMark = readJobsAndAddRemarks(transaction)
        saveJobs(transaction, jobsToMark)
        return mixedJobs
    }

    private fun readJobsAndAddRemarks(transaction: Transaction): List<PrintOrder> {

        var documentRef: DocumentReference
        var snapshot: DocumentSnapshot
        var printOrder: PrintOrder
        val result = LinkedList<PrintOrder>()
        for (job in jobs) {
            documentRef = database.poReference(destinationId, job.documentId())
            snapshot = transaction.get(documentRef)
            if (snapshot.exists()) {
                printOrder = snapshot.toPrintOrder()
                if (printOrder.pendingRemarks != pendingRemark) {
                    printOrder.pendingRemarks = pendingRemark
                    result.add(printOrder)
                } else
                    mixedJobs = true
            }
        }
        return result
    }

    private fun saveJobs(transaction: Transaction, jobsToSave: List<PrintOrder>) {
        var documentRef: DocumentReference
        for (job in jobsToSave) {
            documentRef = database.poReference(destinationId, job.documentId())
            transaction.set(documentRef, job)
        }
    }
}
