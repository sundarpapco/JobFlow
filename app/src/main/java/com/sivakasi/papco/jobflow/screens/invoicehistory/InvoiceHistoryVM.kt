package com.sivakasi.papco.jobflow.screens.invoicehistory

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
class InvoiceHistoryVM @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val pagingFlow=Pager(
        config = PagingConfig(
            pageSize = 100,
            prefetchDistance = 50,
            enablePlaceholders = false,
            initialLoadSize = 100
        )
    ){
        InvoiceHistoryPageSource(repository)
    }.flow.cachedIn(viewModelScope)

}