package com.sivakasi.papco.jobflow.screens.clients.history

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun EntryProviderScope<NavKey>.clientHistoryEntry(
    backStack: NavBackStack<NavKey>
) {
    entry<AppGraph.ClientHistory> { key ->

        val viewModel: ClientHistoryVM =
            hiltViewModel<ClientHistoryVM>().apply { clientId = key.client.id }

        ClientHistoryScreen(
            client = key.client,
            viewModel = viewModel,
            onItemClicked = {
                viewModel.observePrintOrder(it)
                backStack.add(AppGraph.ViewPrintOrder(it.printOrderNumber))
            },
            onBackPressed = { backStack.removeLastOrNull() }
        )
    }
}

@ExperimentalCoroutinesApi

@Composable
fun ClientHistoryScreen(
    client: Client,
    viewModel: ClientHistoryVM,
    onItemClicked: (SearchModel) -> Unit,
    onBackPressed: () -> Unit
) {

    val history = viewModel.clientHistory.collectAsLazyPagingItems()
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = client.name,
                subtitle = stringResource(R.string.client_history),
                navigationIcon = {
                    IconButton(
                        onClick = onBackPressed
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PaginatedSearchModelListScreen(
            history,
            onResultClicked = onItemClicked,
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            viewModel.userUpdatedItem
        )
    }

}

