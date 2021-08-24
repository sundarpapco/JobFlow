package com.sivakasi.papco.jobflow.screens.clients.history.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.clients.history.ClientHistoryVM
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun ClientHistoryScreen(viewModel: ClientHistoryVM,onItemClicked:(SearchModel)->Unit) {

    val history = viewModel.clientHistory.collectAsLazyPagingItems()
    PaginatedSearchModelListScreen(history, onResultClicked = onItemClicked)
}

