package com.sivakasi.papco.jobflow.extensions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sivakasi.papco.jobflow.data.DatabaseContract

fun FirebaseFirestore.poReference(destinationId: String, poId: String): DocumentReference =
    collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(destinationId)
        .collection(DatabaseContract.COLLECTION_JOBS)
        .document(poId)

fun FirebaseFirestore.destinationReference(destinationId: String): DocumentReference =
    collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(destinationId)

