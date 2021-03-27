package com.sivakasi.papco.jobflow.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.sivakasi.papco.jobflow.data.*

class CreatePrintOrderTransaction(private val printOrder: PrintOrder) :
    Transaction.Function<Boolean> {

    private val database = FirebaseFirestore.getInstance()

    //References
    private val poNumberRef = database.collection(DatabaseContract.COLLECTION_COUNTERS)
        .document(DatabaseContract.DOCUMENT_COUNTER_PO_NO)

    private val plateNumberRef = database.collection(DatabaseContract.COLLECTION_COUNTERS)
        .document(DatabaseContract.DOCUMENT_COUNTER_RID)

    private val destinationRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
        .document(DatabaseContract.DOCUMENT_DEST_UNALLOCATED)

    //Initial Value Documents
    private var poNumber = Counter()
    private var plateNumber = Counter()
    private var destination = Destination()


    override fun apply(transaction: Transaction): Boolean {

        //Read all the documents

        val poNumberDocument = transaction.get(poNumberRef)
        val plateNumberDocument = transaction.get(plateNumberRef)
        val destinationDocument = transaction.get(destinationRef)

        if (poNumberDocument.exists())
            poNumber = poNumberDocument.toObject(Counter::class.java)!!

        if (plateNumberDocument.exists())
            plateNumber = plateNumberDocument.toObject(Counter::class.java)!!

        if (destinationDocument.exists())
            destination = destinationDocument.toObject(Destination::class.java)!!


        //Make changes in all read documents

        poNumber.value++
        plateNumber.value++
        destination.jobCount++
        destination.runningTime += printOrder.printingDetail.runningMinutes
        destination.name = DatabaseContract.DOCUMENT_DEST_UNALLOCATED

        printOrder.printOrderNumber = poNumber.value

        //Update the RID number only when its a new job and its our plate
        if (hasToUpdatePlateNumber())
            printOrder.plateMakingDetail.plateNumber = plateNumber.value


        //Write All Values

        /*Creating the printOrder reference alone here. Because the print order document id depends on
        the print order number which was dynamically assigned just in the above statement.
         */
        val printOrderRef = database.collection(DatabaseContract.COLLECTION_DESTINATIONS)
            .document(DatabaseContract.DOCUMENT_DEST_UNALLOCATED)
            .collection(DatabaseContract.COLLECTION_JOBS)
            .document(printOrder.documentId())

        transaction.set(poNumberRef, poNumber)
        if (hasToUpdatePlateNumber())
            transaction.set(plateNumberRef, plateNumber)
        transaction.set(destinationRef, destination)
        transaction.set(printOrderRef, printOrder)

        return true

    }

    private fun hasToUpdatePlateNumber(): Boolean =
        printOrder.jobType == PrintOrder.TYPE_NEW_JOB &&
                printOrder.plateMakingDetail.plateNumber != PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE

}


