package com.sivakasi.papco.jobflow.screens.invoicehistory

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.SearchModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class InvoiceHistoryPageSource(
    private val repository: Repository
) : PagingSource<DocumentSnapshot, SearchModel>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, SearchModel>): DocumentSnapshot? {
        //This paging source cannot and will not invalidate
        return null
    }

    /*
    In this pagination, the Key is the completionTime in the PrintOrder
     */
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, SearchModel> {
        return repository.invoiceHistory((params))
    }
}