package com.sivakasi.papco.jobflow.screens.clients.history

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.models.SearchModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ClientHistoryPagingSource(
    private val clientId:Int,
    private val repository: Repository
): PagingSource<DocumentSnapshot,SearchModel>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, SearchModel>): DocumentSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, SearchModel> {
        return repository.clientHistory(clientId,params)
    }
}