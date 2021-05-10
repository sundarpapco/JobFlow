package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.poReference
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.models.SearchModel
import java.util.*

class MarkAsPendingTransaction(
    private val destinationId: String,
    private val pendingRemark:String,
    private val jobs: List<PrintOrderUIModel>
) : Transaction.Function<Boolean> {

    val database = FirebaseFirestore.getInstance()

    override fun apply(transaction: Transaction): Boolean {

        val jobsToMark=readJobsAndAddRemarks(transaction)
        saveJobs(transaction,jobsToMark)
        return true
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
                printOrder = snapshot.toObject(PrintOrder::class.java)!!
                printOrder.pendingRemarks = pendingRemark
                result.add(printOrder)
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
