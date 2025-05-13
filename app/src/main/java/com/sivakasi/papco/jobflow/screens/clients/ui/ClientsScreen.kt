package com.sivakasi.papco.jobflow.screens.clients.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.screens.common.SingleLineListItem
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalFoundationApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@Composable
fun ClientsScreen(
    screenState: ClientScreenState,
    isSelectionMode: Boolean,
    onClientSelected: (Int, String) -> Unit,
    onClientEdit: (Int, String) -> Unit,
    onClientAdd: (String) -> Unit,
    onBackPressed:()->Unit
) {

    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (isSelectionMode)
                    stringResource(R.string.select_client)
                else
                    stringResource(R.string.clients),

                subtitle = if(screenState.clientList.isEmpty())
                    null
                else
                    stringResource(R.string.xx_clients,screenState.clientList.size),

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
        },
        floatingActionButton = {
            if (!isSelectionMode)
                FloatingActionButton(
                    backgroundColor = MaterialTheme.colors.primary,
                    onClick = { screenState.showAddClientDialog() }
                ) {
                    Icon(Icons.Filled.Add, "Client Add Button")
                }
        }
    ) {
        ContentMain(screenState) {

            when {
                screenState.isLoading -> {
                    LoadingScreen()
                }

                screenState.loadingError != null -> {
                    InformationScreen(screenState.loadingError!!)
                }

                else -> {
                    ClientsList(screenState.clientList) {
                        if (isSelectionMode)
                            onClientSelected(it.id, it.name.text)
                        else
                            screenState.showEditClientDialog(it.id, it.name.text)
                    }
                }
            }
        }
    }


    screenState.dialogState?.let { dialogState ->
        TextInputDialog(
            dialogState = dialogState,
            onPositiveClick = { name ->
                if (dialogState.data == null)
                    onClientAdd(name)
                else
                    onClientEdit(dialogState.data!!, name)
            },
            onNegativeClick = { screenState.hideDialog() }
        )
    }
}


@Composable
private fun ContentMain(
    screenState: ClientScreenState,
    content: @Composable () -> Unit
) {
    val query by screenState.query.collectAsState()
    val searchBarFocus = remember { FocusRequester() }
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                query = query,
                onQueryChange = { screenState.query.value = it },
                Modifier.focusRequester(searchBarFocus)
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }
        }

    }

    DisposableEffect(Unit) {
        searchBarFocus.requestFocus()
        onDispose { }
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
                Icon(
                    Icons.Outlined.Close,
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
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = modifier.fillMaxSize()
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, FlowPreview::class,
    ExperimentalCoroutinesApi::class
)
@Preview
@Composable
private fun PreviewClientsList() {

    val context = LocalContext.current
    val clients = remember {
        listOf(
            ClientUIModel(1, AnnotatedString("Sundaravel")),
            ClientUIModel(2, AnnotatedString("Madhana"))
        )
    }

    val screenState = remember {
        ClientScreenState(context).apply {
            loadClientList(clients)
        }
    }

    JobFlowTheme {
        ClientsScreen(
            screenState = screenState,
            isSelectionMode = false,
            onClientSelected = {_,_-> },
            onClientEdit = {_,_-> },
            onClientAdd = {},
            onBackPressed = {}
        )
    }

}

