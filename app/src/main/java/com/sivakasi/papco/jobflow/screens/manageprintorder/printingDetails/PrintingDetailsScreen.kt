package com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.util.Duration


@Composable
fun PrintingDetailsScreen(
    screenState: PrintingDetailsScreenState,
    onNext: () -> Unit,
    onClose:()->Unit
) {
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (screenState.isEditMode)
                    stringResource(R.string.create_job)
                else
                    stringResource(R.string.edit_job),
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
                    onClick = {
                        if(screenState.validate())
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

        PrintingDetailsScreenContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp),
                screenState = screenState
            )
    }

    if (screenState.isRunningTimeDialogShowing) {
        RunningTimeDialog(
            runningTime = screenState.runningMinutes,
            hasSpotColours = screenState.hasSpotColours,
            onSave = { duration, hasSpot ->
                screenState.runningMinutes = duration.inMinutes()
                screenState.hasSpotColours = hasSpot
                screenState.isRunningTimeDialogShowing = false
            },
            onDismiss = { screenState.isRunningTimeDialogShowing = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun PrintingDetailsScreenContent(
    modifier: Modifier = Modifier,
    screenState: PrintingDetailsScreenState
) {

    val scrollState = rememberScrollState()
    val runningTime by remember {
        derivedStateOf {
            Duration.fromMinutes(screenState.runningMinutes).toString()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        val focusManager = LocalFocusManager.current
        val colorsFocus = remember { FocusRequester() }

        Text(
            stringResource(R.string.printing_details),
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(colorsFocus)
                .onFocusChanged {
                    if (it.isFocused) {
                        val text = screenState.colors.text
                        screenState.colors = screenState.colors.copy(
                            selection = TextRange(0, text.length)
                        )
                    }
                },
            value = screenState.colors,
            onValueChange = {
                screenState.coloursError?.let {
                    screenState.coloursError = null
                }
                screenState.colors = it
            },
            label = stringResource(R.string.colours),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            error = screenState.coloursError
        )

        if (screenState.isCorrectionAllowed)
            JobFlowTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused) {
                            val text = screenState.correction.text
                            screenState.correction = screenState
                                .correction
                                .copy(selection = TextRange(0, text.length))
                        }
                    },
                value = screenState.correction,
                onValueChange = {
                    screenState.correction = it
                },
                label = stringResource(R.string.correction),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                })
            )

        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = screenState.printingInstructions,
            onValueChange = {
                screenState.printingInstructions = it
            },
            label = stringResource(R.string.printing_instructions),
            minLines = 10,
            maxLines = 10
        )

        JobFlowTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    screenState.runningTimeError = null
                    screenState.isRunningTimeDialogShowing = true
                },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = LocalContentColor.current,
                disabledBorderColor = MaterialTheme.colors.secondaryVariant,
                disabledLeadingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
            ),
            value = runningTime,
            onValueChange = {},
            label = stringResource(R.string.running_time),
            singleLine = true,
            readOnly = true,
            enabled = false,
            error = screenState.runningTimeError
        )

        LaunchedEffect(Unit) {
            colorsFocus.requestFocus()
        }
    }
}

@Preview
@Composable
private fun PreviewPrintingDetailsScreen() {

    val context = LocalContext.current
    val screenState = remember {
        PrintingDetailsScreenState(context)
    }
    JobFlowTheme {
        PrintingDetailsScreen(
            screenState = screenState,
            onNext = {},
            onClose = {}
        ) 
    }

}