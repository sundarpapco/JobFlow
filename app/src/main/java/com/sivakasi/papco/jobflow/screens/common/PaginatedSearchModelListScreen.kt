package com.sivakasi.papco.jobflow.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.clients.ui.InformationScreen
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen


@ExperimentalMaterialApi
@Suppress("UNCHECKED_CAST")
@Composable
fun PaginatedSearchModelListScreen(
    data: LazyPagingItems<SearchModel>,
    onResultClicked: (SearchModel) -> Unit
) {

    when (data.loadState.refresh) {

        is LoadState.Loading -> {
            LoadingScreen()
        }

        is LoadState.Error -> {
            InformationScreen(
                message = (data.loadState.refresh as LoadState.Error).error.message
                    ?: stringResource(id = R.string.error_unknown_error)
            )
        }

        is LoadState.NotLoading -> {
            HistoryList(history = data, onResultClicked = onResultClicked)
        }

        else -> {}
    }
}


@ExperimentalMaterialApi
@Composable
private fun HistoryList(
    history: LazyPagingItems<SearchModel>,
    onResultClicked: (SearchModel) -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {


        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history, key = { it.printOrderNumber }) {
                it?.let { item ->
                    SearchListItem(item, onResultClicked)
                }
            }

            item{
                when (history.loadState.append) {
                    is LoadState.Loading -> {
                        LoadingListItem()
                    }
                    is LoadState.Error -> {
                        ErrorListItem{
                            history.retry()
                        }
                    }
                    else->{}
                }
            }
        }

    }
}

@ExperimentalMaterialApi
@Composable
private fun LoadingListItem() {

    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            CircularProgressIndicator()
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun ErrorListItem(
    onRetry:()->Unit
) {

    Card(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = onRetry
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Icon(Icons.Filled.Refresh,"Refresh List")
        }
    }
}