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
class AlgoliaSearchVM @Inject constructor(
    val application: Application,
    val repository: Repository
) : ViewModel() {

    private val algoliaClient = AlgoliaClientImpl()
    var query: String? by mutableStateOf(null)
    private var dataSource: AlgoliaDataSource? = null
    var userUpdatedItem: Event<SearchModel>? by mutableStateOf(null)
    private var printOrderObservingJob: Job? = null

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
        printOrderObservingJob?.cancel()
        userUpdatedItem = null
        //begin search only when user changes the query. Pressing search without changing the
        //query wont trigger a new search
        if (query != dataSource?.query) {
            this.query = query.trim()
            dataSource?.invalidate()
        }

    }


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