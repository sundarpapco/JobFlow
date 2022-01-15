package com.sivakasi.papco.jobflow.screens.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class AlgoliaSearchVM @Inject constructor(
    val application: Application
) : ViewModel() {

    private val algoliaClient = AlgoliaClientImpl()
    var initialLoading = true
    var query: String? by mutableStateOf(null)
    var dataSource: AlgoliaDataSource? = null

    val pagingFlow = Pager(
        config = PagingConfig(
            pageSize = 100,
            prefetchDistance = 50,
            enablePlaceholders = false,
            initialLoadSize = 100
        )
    ) {
        dataSource = AlgoliaDataSource(application, algoliaClient, query)
        dataSource!!
    }.flow.cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    fun search(query: String) {
        //begin search only when user changes the query. Pressing search without changing the
        //query wont trigger a new search
        if (query != dataSource?.query) {
            this.query = query.trim()
            dataSource?.invalidate()
        }

    }

}