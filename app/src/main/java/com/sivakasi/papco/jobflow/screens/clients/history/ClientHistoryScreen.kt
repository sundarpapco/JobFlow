package com.sivakasi.papco.jobflow.screens.clients.history

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun ClientHistoryScreen(
    client: Client,
    viewModel: ClientHistoryVM,
    onItemClicked:(SearchModel)->Unit,
    onBackPressed:()->Unit
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
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            viewModel.userUpdatedItem
        )
    }

}

