package com.sivakasi.papco.jobflow.screens.clients.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragmentVM
import com.sivakasi.papco.jobflow.screens.common.SingleLineListItem
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalFoundationApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@Composable
@Suppress("UNCHECKED_CAST")
fun ClientsScreen(viewModel: ClientsFragmentVM, selectionMode: Boolean = false) {

    val loadingState by viewModel.clientsList.observeAsState(initial = LoadingStatus.Loading(""))
    val newClientDialog = viewModel.newClientDialogState
    val editClientDialog = viewModel.editClientDialogState
    val query = viewModel.query

    Scaffold(
        floatingActionButton = {
            if (!selectionMode)
                FloatingActionButton(
                    backgroundColor = MaterialTheme.colors.primary,
                    onClick = viewModel::onFabClicked
                ) {
                    Icon(Icons.Filled.Add, "Client Add Button")
                }
        }
    ) {
        ContentMain(query = query.value, onQueryChange = viewModel::onQueryChange) {
            LoadScreensByLoadingState<List<ClientUIModel>>(loadingState = loadingState) {
                ClientsList(
                    clientsList = it,
                    onItemClicked = if (selectionMode) viewModel::onClientSelected else viewModel::onClientClicked
                )
            }
        }
    }


    newClientDialog.value?.let {
        TextInputDialog(
            dialogState = it,
            onPositiveClick = viewModel::onAddClient,
            onNegativeClick = viewModel::cancelNewClientDialog
        )
    }

    editClientDialog.value?.let {
        TextInputDialog(
            dialogState = it,
            onPositiveClick = viewModel::onUpdateClient,
            onNegativeClick = viewModel::cancelEditClientDialog
        )
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun <T> LoadScreensByLoadingState(
    loadingState: LoadingStatus,
    onLoading: @Composable () -> Unit = { LoadingScreen() },
    onError: @Composable (String) -> Unit = { InformationScreen(message = it) },
    onSuccess: @Composable (T) -> Unit
) {
    when (loadingState) {
        is LoadingStatus.Loading -> {
            onLoading()
        }

        is LoadingStatus.Error -> {
            val errorMsg =
                loadingState.exception.message ?: stringResource(id = R.string.error_unknown_error)
            onError(errorMsg)
        }

        is LoadingStatus.Success<*> -> {
            val data = loadingState.data as T
            onSuccess(data)
        }
    }
}


@Composable
private fun ContentMain(
    query: String,
    onQueryChange: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val searchBarFocus = remember { FocusRequester() }
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                Modifier.focusRequester(searchBarFocus)
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }
        }

    }

    DisposableEffect(Unit){
        searchBarFocus.requestFocus()
        onDispose {  }
    }

}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
        label = { Text("Search Client") },
        leadingIcon = {
            Icon(Icons.Outlined.Search, "Search", Modifier.alpha(ContentAlpha.medium))
        },
        trailingIcon = {
            if (query.isNotBlank())
                Icon(Icons.Outlined.Close,
                    "Clear Query",
                    Modifier
                        .alpha(ContentAlpha.medium)
                        .clickable {
                            onQueryChange("")
                        }
                )
        }
    )
}


@Composable
private fun ClientsList(
    clientsList: List<ClientUIModel>,
    onItemClicked: (ClientUIModel) -> Unit
) {

    if (clientsList.isEmpty())
        InformationScreen(message = stringResource(id = R.string.no_clients_found))
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            itemsIndexed(
                clientsList,
                key = { _, item -> item.id }) { index, client: ClientUIModel ->
                SingleLineListItem(
                    data = client,
                    textBlock = { it.name },
                    onClick = onItemClicked
                )
                if (index < clientsList.size - 1)
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                else
                    Spacer(Modifier.height(60.dp))
            }
        }
}


@Composable
fun LoadingScreen() {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.requiredSize(60.dp)
            )
        }
    }
}

@Composable
fun InformationScreen(
    message: String
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingScreen() {
    JobFlowTheme {
        LoadingScreen()
    }
}

@Preview
@Composable
private fun PreviewClientsList(){
    
    val clients = remember{
        listOf(
            ClientUIModel(1, AnnotatedString("Sundaravel")),
            ClientUIModel(2, AnnotatedString("Madhana"))
        )
    }
    
    JobFlowTheme {
        ClientsList(clientsList = clients, onItemClicked = {})
    }
    
}

