package com.sivakasi.papco.jobflow.screens.manageprintorder.addJob

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowCircularProgressBar
import com.sivakasi.papco.jobflow.ui.JobFlowRadioButton
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar

@Composable
fun AddPrintOrderScreen(
    screenState: AddJobScreenState,
    onNext: () -> Unit,
    onClosePressed: () -> Unit,
    onOldPlateNotFoundContinuation: () -> Unit
) {
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = stringResource(R.string.create_job),
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
                    enabled = !screenState.isWaiting,
                    onClick = onNext
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
        )
    }

    if (screenState.isPONotFoundDialogShowing)
        JobFlowAlertDialog(
            title = stringResource(R.string.po_not_found),
            message = stringResource(R.string.confirmation_old_print_order_not_found_proceed),
            positiveButtonText = stringResource(R.string.proceed),
            negativeButtonText = stringResource(R.string.cancel),
            onPositiveClick = onOldPlateNotFoundContinuation,
            onDismissListener = { screenState.hideIsPONotFoundDialog() },
            onNegativeClick = { screenState.hideIsPONotFoundDialog() }
        )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun AddJobScreenContent(
    screenState: AddJobScreenState,
    modifier: Modifier = Modifier
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
                onValueChange = { screenState.ridNumber = it },
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
                    onSearch = {

                    }
                )
            )
            Text(
                text = stringResource(R.string.blank_if_party_plate),
                color = MaterialTheme.colors.secondaryVariant
            )
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
            onNext = {},
            onClosePressed = {},
            onOldPlateNotFoundContinuation = {}
        )
    }
}