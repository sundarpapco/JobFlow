package com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.intNumber
import com.sivakasi.papco.jobflow.screens.manageprintorder.RunningTimeExpressionChecker
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.util.Duration

@Composable
fun RunningTimeDialog(
    runningTime: Int,
    hasSpotColours: Boolean,
    onSave: (Duration, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        RunningTimeDialogContent(
            runningTime,
            hasSpotColours,
            onSave = onSave
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun RunningTimeDialogContent(
    runningTime: Int,
    hasSpotColours: Boolean,
    onSave: (Duration, Boolean) -> Unit
) {

    var runningHours by remember(runningTime) {
        val duration = Duration.fromMinutes(runningTime)
        mutableStateOf(TextFieldValue(duration.hours.toString()))
    }

    var runningMinutes by remember(runningTime) {
        val duration = Duration.fromMinutes(runningTime)
        mutableStateOf(TextFieldValue(duration.minutes.toString()))
    }

    var durationError: String? by rememberSaveable {
        mutableStateOf(null)
    }

    var expression by remember(runningTime) {
        mutableStateOf(TextFieldValue(""))
    }

    var expressionError: String? by rememberSaveable {
        mutableStateOf(null)
    }

    var spotColours by remember(hasSpotColours) {
        mutableStateOf(hasSpotColours)
    }

    val focusManager = LocalFocusManager.current
    val expressionFocus = remember {
        FocusRequester()
    }

    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.running_time),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {

                JobFlowTextField(
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            if (it.isFocused) {
                                runningHours = runningHours.copy(
                                    selection = TextRange(0, runningHours.text.length)
                                )
                            }
                        },
                    value = runningHours,
                    onValueChange = {
                        runningHours = it
                    },
                    label = stringResource(R.string.hours),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    )
                )

                JobFlowTextField(
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            if (it.isFocused) {
                                runningMinutes = runningMinutes.copy(
                                    selection = TextRange(0, runningMinutes.text.length)
                                )
                            }
                        },
                    value = runningMinutes,
                    onValueChange = {
                        durationError = null
                        runningMinutes = it
                    },
                    label = stringResource(R.string.minutes),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    error = durationError
                )

            }

            JobFlowTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(expressionFocus)
                    .onFocusChanged {
                        if (it.isFocused) {
                            val text = expression.text
                            expression = expression.copy(
                                selection = TextRange(0, text.length)
                            )
                        }
                    },
                value = expression,
                onValueChange = {
                    expressionError = null
                    expression = it
                },
                label = stringResource(R.string.expression),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                ),
                error = expressionError
            )
            

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val context = LocalContext.current

                Checkbox(
                    checked = spotColours,
                    onCheckedChange = { spotColours = !spotColours }
                )

                Text(
                    modifier = Modifier.weight(2f),
                    text = stringResource(R.string.has_spot_colours)
                )

                Button(
                    onClick = {
                        val expressionText = expression.text.trim()
                        if (expressionText.isNotBlank()) {
                            val runningTimeExpressionChecker = RunningTimeExpressionChecker(expressionText)
                            if (runningTimeExpressionChecker.isValid) {
                                val duration = runningTimeExpressionChecker.totalTime()
                                val spot = runningTimeExpressionChecker.hasExtraColour
                                onSave(duration, spot)
                            } else
                                expressionError =
                                    context.getString(R.string.error_invalid_expression)
                        } else {

                            if (runningMinutes.text.intNumber(0) > 59)
                                durationError = context.getString(R.string.error_invalid_minutes)
                            else {
                                val duration = Duration(
                                    runningHours.text.intNumber(0),
                                    runningMinutes.text.intNumber(0)
                                )
                                if (duration.inMinutes() == 0)
                                    durationError =
                                        context.getString(R.string.error_invalid_minutes)
                                else
                                    onSave(duration, spotColours)
                            }
                        }

                    }
                ) {
                    Text(
                        text = stringResource(R.string.save)
                    )
                }
            }

            LaunchedEffect(Unit) {
                expressionFocus.requestFocus()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewRunningDialogContent() {
    JobFlowMaterial3Theme {
        RunningTimeDialogContent(90, true) { _, _ ->

        }
    }
}