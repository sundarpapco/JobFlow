package com.sivakasi.papco.jobflow.screens.manageprintorder.addJob

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.extensions.intNumber
import com.sivakasi.papco.jobflow.nav3.graph.PrintOrderGraph
import com.sivakasi.papco.jobflow.nav3.replaceLastOrAdd
import com.sivakasi.papco.jobflow.screens.manageprintorder.ManagePrintOrderVM
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowCircularProgressBar
import com.sivakasi.papco.jobflow.ui.JobFlowRadioButton
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun EntryProviderScope<NavKey>.addPOScreenEntry(
    viewModel: ManagePrintOrderVM,
    backStack: NavBackStack<NavKey>,
    onExitFlow:()->Unit
) {
    entry<PrintOrderGraph.AddPO> { key ->

        val loadedJob by viewModel.loadedJob.collectAsStateWithLifecycle()
        var isAlreadyLoaded = rememberSaveable { false }

        AddPrintOrderScreen(
            screenState = viewModel.addJobScreenState,
            isEditMode = key.editingPONumber != null,
            onCreateNewJob = viewModel::createNewJob,
            onCreateRepeatJob = viewModel::createRepeatJob,
            onLoadRepeatJob = viewModel::loadJobByPlateNumber,
            onClose = onExitFlow
        )

        //Initialize the Auto repeat mode or Editing Mode
        LaunchedEffect(Unit) {
            if (!isAlreadyLoaded) {
                if (key.autoRepeat) {
                    requireNotNull(key.editingPONumber) { "Invalid PO number provided in auto repeat mode" }
                    //Search and load from repo using the provided PO number and not plate number
                    viewModel.loadJobByPONumber(key.editingPONumber)
                } else {
                    key.editingPONumber?.let {
                        viewModel.isEditMode = true
                        viewModel.editingPrintOrderParentDestinationId = key.parentDestinationId
                        viewModel.loadJobByPONumber(it)
                    }
                }
                isAlreadyLoaded = true
            }
        }

        //Navigate to Next screen once a valid PO has been loaded
        LaunchedEffect(loadedJob) {
            loadedJob?.let {
                backStack.replaceLastOrAdd(PrintOrderGraph.JobDetails)
            }
        }
    }


}

@Composable
fun AddPrintOrderScreen(
    screenState: AddJobScreenState,
    isEditMode: Boolean,
    onCreateNewJob: () -> Unit,
    onCreateRepeatJob: (Int) -> Unit,
    onLoadRepeatJob: (Int) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (isEditMode)
                    stringResource(R.string.edit_job)
                else
                    stringResource(R.string.create_job),
                navigationIcon = {
                    IconButton(
                        onClick = onClose
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
                    enabled = !screenState.isWaiting,
                    onClick = {
                        onFormSubmit(
                            context,
                            screenState,
                            onCreateNewJob,
                            onCreateRepeatJob,
                            onLoadRepeatJob
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.next)
                    )
                }
            }
        }
    ) { paddingValues ->
        AddJobScreenContent(
            screenState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp),
            onSearch = {
                onFormSubmit(
                    context,
                    screenState,
                    onCreateNewJob,
                    onCreateRepeatJob,
                    onLoadRepeatJob
                )
            }
        )
    }

    if (screenState.isPONotFoundDialogShowing)
        JobFlowAlertDialog(
            title = stringResource(R.string.po_not_found),
            message = stringResource(R.string.confirmation_old_print_order_not_found_proceed),
            positiveButtonText = stringResource(R.string.proceed),
            negativeButtonText = stringResource(R.string.cancel),
            onPositiveClick = {
                val plateNumber = screenState.ridNumber.intNumber(
                    PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE
                )
                onCreateRepeatJob(plateNumber)
            },
            onDismissListener = { screenState.hideIsPONotFoundDialog() },
            onNegativeClick = { screenState.hideIsPONotFoundDialog() }
        )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun AddJobScreenContent(
    screenState: AddJobScreenState,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.new_print_order),
            style = MaterialTheme.typography.h4
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                JobFlowRadioButton(
                    isSelected = screenState.isNewJob,
                    title = stringResource(R.string.new_job),
                    onClick = { screenState.isNewJob = true },
                    enabled = !screenState.isWaiting
                )

                JobFlowRadioButton(
                    isSelected = !screenState.isNewJob,
                    title = stringResource(R.string.repeat_job),
                    onClick = {
                        screenState.isNewJob = false
                    },
                    enabled = !screenState.isWaiting
                )
            }

            if (screenState.isWaiting)
                JobFlowCircularProgressBar(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.one_moment_please)
                )

        }

        if (!screenState.isNewJob) {
            JobFlowTextField(
                value = screenState.ridNumber,
                onValueChange = {
                    screenState.ridError = null
                    if (it.isDigitsOnly())
                        screenState.ridNumber = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                label = stringResource(R.string.rid),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        tint = MaterialTheme.colors.primary,
                        contentDescription = "Search Icon"
                    )
                },
                enabled = !screenState.isWaiting,
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                error = screenState.ridError
            )
            Text(
                text = stringResource(R.string.blank_if_party_plate),
                color = MaterialTheme.colors.secondaryVariant
            )
        }

    }
}

private fun onFormSubmit(
    context: Context,
    screenState: AddJobScreenState,
    onCreateNewJob: () -> Unit,
    onCreateRepeatJob: (Int) -> Unit,
    onLoadRepeatJob: (Int) -> Unit
) {

    if (screenState.isNewJob) {
        onCreateNewJob()
        return
    }

    val plateNumber = screenState.ridNumber.intNumber(PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)

    when (plateNumber) {

        PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE -> {
            onCreateRepeatJob(plateNumber)
        }

        0 -> {
            screenState.ridError = context.getString(R.string.invalid_plate_number)
        }

        else -> {
            onLoadRepeatJob(plateNumber)
        }
    }
}

@Preview
@Composable
private fun PreviewNewJobScreenContent() {

    val screenState = remember {
        AddJobScreenState()
    }

    JobFlowTheme {
        AddPrintOrderScreen(
            screenState,
            isEditMode = true,
            onCreateNewJob = {},
            onCreateRepeatJob = {},
            onLoadRepeatJob = {},
            onClose = {}
        )
    }
}