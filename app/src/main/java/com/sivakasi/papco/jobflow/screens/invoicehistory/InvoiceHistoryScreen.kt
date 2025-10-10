package com.sivakasi.papco.jobflow.screens.invoicehistory

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
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun InvoiceHistoryScreen(
    viewModel: InvoiceHistoryVM,
    onItemClicked: (SearchModel) -> Unit,
    onBackPressed: ()->Unit
) {
    val history = viewModel.pagingFlow.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = stringResource(R.string.invoice_history),
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
    ) {paddingValues ->
        PaginatedSearchModelListScreen(
            data = history,
            onResultClicked = onItemClicked,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            viewModel.userUpdatedItem
        )
    }

}


