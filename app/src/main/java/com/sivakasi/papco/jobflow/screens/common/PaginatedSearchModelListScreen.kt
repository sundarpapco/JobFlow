package com.sivakasi.papco.jobflow.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.sivakasi.papco.jobflow.util.Event

/*
Lazy Column list which will display the list from the paging 3 API
Will display loading progress bar on initial as well as subsequent loads, error etc.,

**This list is not realtime updated However using the realtimeUpdatedItem parameter we can simulate it

This list will render the pagination loaded items on screen if the realTimeUpdatedItem is null
If its not null, then that item is being copied in to the actual pagination list while rendering that
particular item in the list. When copied, realTimeUpdatedItem parameter is marked as handled so that
the item will not be copied again and again whenever that particular item is rendered on the screen

 */


@ExperimentalMaterialApi
@Suppress("UNCHECKED_CAST")
@Composable
fun PaginatedSearchModelListScreen(
    data: LazyPagingItems<SearchModel>,
    onResultClicked: (SearchModel) -> Unit,
    modifier: Modifier = Modifier,
    realTimeUpdatedItem: Event<SearchModel>? = null
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
            HistoryList(
                history = data,
                onResultClicked = onResultClicked,
                modifier = modifier,
                realTimeUpdatedItem = realTimeUpdatedItem
            )
        }

        else -> {}
    }
}


@ExperimentalMaterialApi
@Composable
private fun HistoryList(
    history: LazyPagingItems<SearchModel>,
    onResultClicked: (SearchModel) -> Unit,
    modifier: Modifier = Modifier,
    realTimeUpdatedItem: Event<SearchModel>? = null
) {

    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {

        if (listState.firstVisibleItemIndex >= 0 || listState.firstVisibleItemScrollOffset != 0) {
            //User has already used and scrolled this list
            if (history.itemCount == 0) {
                /*User has already scrolled the list meaning the item count should be greater than
                what can be shown on screen. But the item count is zero which is a conflict
                This means this is due to caching and we should wait for the actual data to render the list
                 */
                LoadingScreen()

            }
        } else {
            //The user has not scrolled this list, meaning we should display the list if it has valid items
            if (history.itemCount == 0) {
                InformationScreen(message = stringResource(id = R.string.no_results_found))
                return@Surface
            }
        }

        LazyColumn(
            modifier = modifier,
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item(key = "topSpace") {
                Spacer(Modifier.height(16.dp))
            }

            items(history, key = { it.printOrderNumber }) {
                it?.let { item ->

                    realTimeUpdatedItem?.let { updatedItem ->

                        if (!updatedItem.isAlreadyHandled()) {
                            if (item.printOrderNumber == updatedItem.peekData().printOrderNumber) {
                                item.copyValuesFrom(updatedItem.handleEvent())
                            }
                        }

                        SearchListItem(searchModel = item, onClick = onResultClicked)

                    } ?: SearchListItem(item, onResultClicked)
                }
            }

            item {
                when (history.loadState.append) {
                    is LoadState.Loading -> {
                        LoadingListItem()
                    }
                    is LoadState.Error -> {
                        ErrorListItem {
                            history.retry()
                        }
                    }
                    else -> {}
                }
            }

            item(key = "bottomSpace") {
                Spacer(Modifier.height(24.dp))
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
    onRetry: () -> Unit
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
            Icon(Icons.Filled.Refresh, "Refresh List")
        }
    }
}