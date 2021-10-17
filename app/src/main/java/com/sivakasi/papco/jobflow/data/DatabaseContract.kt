package com.sivakasi.papco.jobflow.data

class DatabaseContract {

    companion object{

        const val COLLECTION_DESTINATIONS="Destinations"
        const val COLLECTION_COUNTERS="Counters"
        const val COLLECTION_JOBS="Jobs"
        const val COLLECTION_CLIENTS="Clients"
        const val COLLECTION_USERS="users"

        const val DOCUMENT_DEST_NEW_JOBS="New Jobs"
        const val DOCUMENT_DEST_COMPLETED="Completed"
        const val DOCUMENT_DEST_IN_PROGRESS="In Progress"
        const val DOCUMENT_DEST_CANCELLED="Cancelled"

        const val DOCUMENT_COUNTER_PO_NO="PONumber"
        const val DOCUMENT_COUNTER_RID="RID"
        const val DOCUMENT_COUNTER_CLIENT_ID="ClientId"

    }
}