package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.models.SearchModel

class UpdateJobsBatch(
    private val destinationId: String,
    private val jobList: List<PrintOrderUIModel>
) : WriteBatch.Function {

    private val database=FirebaseFirestore.getInstance()

    override fun apply(batch: WriteBatch) {

        var documentRef:DocumentReference
        val values=HashMap<String,Any>()
        for(job in jobList){
            documentRef=database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
                .document(destinationId)
                .collection(DatabaseContract.COLLECTION_JOBS)
                .document(job.documentId())

            values["listPosition"]=job.listPosition
            batch.update(documentRef,values)
        }

    }
}