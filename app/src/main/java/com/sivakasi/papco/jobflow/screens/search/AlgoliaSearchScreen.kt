package com.sivakasi.papco.jobflow.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.common.PaginatedSearchModelListScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import kotlinx.coroutines.delay

@ExperimentalMaterialApi
@Composable
fun AlgoliaSearchScreen(
    viewModel: AlgoliaSearchVM,
    onItemClicked: (SearchModel) -> Unit,
    onBackPressed: () -> Unit
) {

    val data = viewModel.pagingFlow.collectAsLazyPagingItems()
    val focusRequester = remember { FocusRequester() }

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
                onQuerySubmit = viewModel::search,
                onQueryClear = { viewModel.query = "" },
                onBackPressed = onBackPressed,
                focusRequester = focusRequester
            )

            PaginatedSearchModelListScreen(
                data = data,
                onResultClicked = onItemClicked,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }

    LaunchedEffect(key1 = true) {
        if (viewModel.initialLoading) {
            viewModel.initialLoading = false
            delay(200)
            focusRequester.requestFocus()
        }
    }

}

@Composable
private fun AlgoliaTopABar(
    query: String,
    onQueryChange: (String) -> Unit,
    onQuerySubmit: (String) -> Unit,
    onQueryClear: () -> Unit,
    onBackPressed: () -> Unit,
    focusRequester: FocusRequester
) {

    val focusManager = LocalFocusManager.current

    TopAppBar(
        elevation = 0.dp
    ) {

        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
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
                    focusManager.clearFocus(true)
                    onQuerySubmit(query)
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.primarySurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }


}

@Preview
@Composable
private fun AlgoliaTopBarPreview() {

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    JobFlowTheme {
        AlgoliaTopABar(
            query = query,
            onQueryChange = { query = it },
            onQuerySubmit = {},
            onQueryClear = { query = "" },
            onBackPressed = {},
            focusRequester = focusRequester
        )
    }
}