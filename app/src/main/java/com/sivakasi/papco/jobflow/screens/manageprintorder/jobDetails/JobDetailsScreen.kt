package com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.ClientSelectionPurpose
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.nav3.graph.PrintOrderGraph
import com.sivakasi.papco.jobflow.nav3.util.ResultEffect
import com.sivakasi.papco.jobflow.nav3.util.ResultEventBus
import com.sivakasi.papco.jobflow.screens.manageprintorder.ManagePrintOrderVM
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun EntryProviderScope<NavKey>.jobDetailsScreenEntry(
    viewModel: ManagePrintOrderVM,
    backStack: NavBackStack<NavKey>,
    resultBus: ResultEventBus,
    onExitFlow:()->Unit
) {
    entry<PrintOrderGraph.JobDetails> {

        val screenState = viewModel.jobDetailsScreenState
        val processDeath by viewModel.recoveringFromProcessDeath.collectAsStateWithLifecycle()

        JobDetailsScreen(
            screenState = viewModel.jobDetailsScreenState,
            onSelectClientName = { backStack.add(AppGraph.Client(ClientSelectionPurpose.POCreationOrEditing)) },
            onNext = { backStack.add(PrintOrderGraph.PaperDetails) },
            onClosePressed = onExitFlow
        )

        ResultEffect<Client>(resultBus, AppGraph.Client.SELECTION_KEY) {
            screenState.selectClient(it.id, it.name)
        }

        LaunchedEffect(processDeath) {
            if (processDeath)
                onExitFlow()
        }
    }
}

@Composable
fun JobDetailsScreen(
    screenState: JobDetailsScreenState,
    onSelectClientName: () -> Unit,
    onNext: () -> Unit,
    onClosePressed: () -> Unit
) {
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (screenState.isEditMode)
                    stringResource(R.string.edit_job)
                else
                    stringResource(R.string.create_job),
                navigationIcon = {
                    IconButton(
                        onClick = onClosePressed
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 0.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        if (screenState.validate())
                            onNext()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.next)
                    )
                }
            }
        }
    ) { paddingValues ->
        JobDetailsScreenContent(
            screenState,
            onSelectClientName,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun JobDetailsScreenContent(
    screenState: JobDetailsScreenState,
    onSelectClientName: () -> Unit,
    modifier: Modifier = Modifier
) {

    val jobNameFocus = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.job_details),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = stringResource(R.string.urgent),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.width(12.dp))

            Switch(
                checked = screenState.isUrgent,
                onCheckedChange = { screenState.isUrgent = it }

            )

        }

        JobFlowTextField(
            modifier = Modifier
                .clickable {
                    screenState.clientNameError = null
                    onSelectClientName()
                },
            value = screenState.clientName,
            onValueChange = {
                screenState.clientNameError = null
                screenState.clientName = it
            },
            label = stringResource(R.string.client_name),
            singleLine = true,
            error = screenState.clientNameError,
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = LocalContentColor.current,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        )

        JobFlowTextField(
            modifier = Modifier.focusRequester(jobNameFocus),
            value = screenState.jobName,
            onValueChange = {
                screenState.jobNameError = null
                screenState.jobName = it
            },
            label = stringResource(R.string.job_name),
            singleLine = true,
            error = screenState.jobNameError
        )

        Column {
            JobFlowTextField(
                value = screenState.pendingRemarks,
                onValueChange = {
                    screenState.pendingRemarks = it
                },
                label = stringResource(R.string.pending_remarks),
                singleLine = true
            )
            Text(
                text = stringResource(R.string.blank_if_not_pending),
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (screenState.editingCompletedPO)
            JobFlowTextField(
                value = screenState.invoiceDetail,
                onValueChange = {
                    screenState.invoiceDetail = it
                },
                label = stringResource(R.string.invoice_detail),
                singleLine = true
            )
    }

    LaunchedEffect(Unit) {
        jobNameFocus.requestFocus()
    }
}

@Preview
@Composable
private fun PreviewJobDetailsScreen() {

    val context = LocalContext.current
    val screenState = remember {
        JobDetailsScreenState(context)
    }

    JobFlowMaterial3Theme {
        JobDetailsScreen(
            screenState = screenState,
            onSelectClientName = {},
            onNext = {},
            onClosePressed = {}
        )
    }

}