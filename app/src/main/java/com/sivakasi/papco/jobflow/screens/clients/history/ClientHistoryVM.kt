package com.sivakasi.papco.jobflow.screens.clients.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.sivakasi.papco.jobflow.data.Repository
import com.sivakasi.papco.jobflow.data.toSearchModel
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ClientHistoryVM @Inject constructor(
    private val repository: Repository,
    private val application:Application
) : ViewModel() {

    private var printOrderObservingJob: Job? = null
    var userUpdatedItem:Event<SearchModel>? = null

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
    }.flow.cachedIn(viewModelScope).flowOn(Dispatchers.IO)

    fun observePrintOrder(item: SearchModel) {
        // Cancel any previously observing job
        printOrderObservingJob?.cancel()
        printOrderObservingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.observePrintOrder(item.printOrderNumber)
                    .collect {
                        userUpdatedItem = it?.let { po ->
                            Event(po.printOrder.toSearchModel(application, po.destination.id))
                        }
                    }
            } catch (_: Exception) {
            }
        }
    }

}