package com.sivakasi.papco.jobflow.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
fun EntryProviderScope<NavKey>.searchEntry(
    backStack: NavBackStack<NavKey>
) {
    entry<AppGraph.AlgoliaSearch> {

        val viewModel: AlgoliaSearchVM = hiltViewModel()

        AlgoliaSearchScreen(
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
@ExperimentalMaterialApi
@Composable
fun AlgoliaSearchScreen(
    viewModel: AlgoliaSearchVM,
    onItemClicked: (SearchModel) -> Unit,
    onBackPressed: () -> Unit
) {

    val data = viewModel.pagingFlow.collectAsLazyPagingItems()
    var searchActivated by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            AlgoliaTopABar(
                query = viewModel.query ?: "",
                onQueryChange = { viewModel.query = it },
                onQuerySubmit = {
                    searchActivated = true
                    viewModel.search(it)
                },
                onQueryClear = { viewModel.query = "" },
                onBackPressed = onBackPressed
            )

            if (searchActivated)
                PaginatedSearchModelListScreen(
                    data = data,
                    onResultClicked = onItemClicked,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    realTimeUpdatedItem = viewModel.userUpdatedItem
                )
        }
    }

}

@Composable
private fun AlgoliaTopABar(
    query: String,
    onQueryChange: (String) -> Unit,
    onQuerySubmit: (String) -> Unit,
    onQueryClear: () -> Unit,
    onBackPressed: () -> Unit
) {

    val focusManager = LocalFocusManager.current
    var initialLoading by rememberSaveable { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {

        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back"
            )
        }

        TextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            value = query,
            onValueChange = onQueryChange,
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onQueryClear) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear Query"
                        )
                    }
                }
            },
            placeholder = {
                Text(stringResource(id = R.string.search))
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (query.isNotBlank()) {
                        focusManager.clearFocus(true)
                        onQuerySubmit(query)
                    }
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }

    LaunchedEffect(key1 = true) {
        if (initialLoading) {
            initialLoading = false
            delay(200.milliseconds)
            focusRequester.requestFocus()
        }
    }


}

@Preview
@Composable
private fun AlgoliaTopBarPreview() {

    var query by remember { mutableStateOf("") }

    JobFlowTheme {
        AlgoliaTopABar(
            query = query,
            onQueryChange = { query = it },
            onQuerySubmit = {},
            onQueryClear = { query = "" },
            onBackPressed = {}
        )
    }
}