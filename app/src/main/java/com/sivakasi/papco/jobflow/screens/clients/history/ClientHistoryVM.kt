package com.sivakasi.papco.jobflow.screens.clients.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.sivakasi.papco.jobflow.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ClientHistoryVM @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    var clientId: Int = -1
    val clientHistory = Pager(
        config = PagingConfig(
            pageSize = 100,
            prefetchDistance = 50,
            enablePlaceholders = false,
            initialLoadSize = 100
        )
    ) {
        require(clientId > 0) { "Client id in view model should be set before collecting the clientHistory flow" }
        ClientHistoryPagingSource(clientId, repository)
    }.flow.cachedIn(viewModelScope)

}