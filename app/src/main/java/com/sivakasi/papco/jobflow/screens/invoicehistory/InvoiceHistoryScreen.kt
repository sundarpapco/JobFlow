package com.sivakasi.papco.jobflow.screens.invoicehistory

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun InvoiceHistoryScreen(viewModel: InvoiceHistoryVM, onItemClicked: (SearchModel) -> Unit) {
    val history = viewModel.pagingFlow.collectAsLazyPagingItems()
    PaginatedSearchModelListScreen(
        data = history,
        onResultClicked = onItemClicked,
        modifier = Modifier.padding(horizontal = 16.dp),
        viewModel.userUpdatedItem
    )
}


