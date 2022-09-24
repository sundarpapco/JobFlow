package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.ui.JobFlowRadioButton
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import kotlinx.coroutines.delay

class LaminationDialogState {

    var material: Int by mutableStateOf(Lamination.MATERIAL_PVC)
    var micron: TextFieldValue by mutableStateOf(TextFieldValue("7"))
    var remarks: TextFieldValue by mutableStateOf(TextFieldValue(""))

    fun toLamination(): Lamination = Lamination(material, micron.text.toInt(), remarks.text)

}

class BindingDialogState {
    var binding: Int by mutableStateOf(Binding.TYPE_SADDLE_STITCH)
    var remarks: TextFieldValue by mutableStateOf(TextFieldValue(""))

    fun toBinding(): Binding = Binding(binding, remarks.text)
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun LaminationDialog(
    state: LaminationDialogState,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {

    Dialog(
        onDismissRequest = {
            onNegativeClick()
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        )
    ) {

        LaminationDialogContent(
            state = state,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
    }

}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun LaminationDialogContent(
    state: LaminationDialogState,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {

    val micronFocus = remember { FocusRequester() }
    val remarksFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
        ) {

            Text(
                stringResource(id = R.string.lamination),
                style = MaterialTheme.typography.h5
            )

            Spacer(Modifier.height(16.dp))

            LaminationMaterials(laminationDialogState = state)

            Spacer(modifier = Modifier.height(12.dp))

            JobFlowTextField(
                value = state.micron,
                onValueChange = {
                    if (!it.text.contains("\t"))
                        state.micron = it
                },
                onTabPressed = {
                    remarksFocus.requestFocus()
                },
                error = if (validateLaminationMicron(state.micron.text))
                    null
                else
                    stringResource(id = R.string.error_invalid_micron),
                label = stringResource(id = R.string.micron),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(micronFocus)
                    .onFocusChanged {
                        if (it.isFocused)
                            state.micron = state.micron.copy(
                                selection = TextRange(
                                    0,
                                    state.micron.text.length
                                )
                            )
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        remarksFocus.requestFocus()
                    }
                )
            )

            Spacer(Modifier.height(8.dp))

            JobFlowTextField(
                value = state.remarks,
                onValueChange = {

                    if (!it.text.contains("\t"))
                        state.remarks = it
                },
                onTabPressed = {
                    micronFocus.requestFocus()
                },
                label = stringResource(id = R.string.remarks),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(remarksFocus)
                    .onFocusChanged {
                        if (it.isFocused)
                            state.remarks = state.remarks.copy(
                                selection = TextRange(
                                    0,
                                    state.remarks.text.length
                                )
                            )
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(Modifier.height(16.dp))

            DialogButtons(
                positiveButtonText = stringResource(id = R.string.save),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = onPositiveClick,
                onNegativeClick = onNegativeClick,
                positiveButtonEnabled = {
                    validateLaminationMicron(state.micron.text)
                }
            )
        }
    }

    LaunchedEffect(true) {
        delay(200)
        micronFocus.requestFocus()
    }

}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun BindingDialog(
    state: BindingDialogState,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            onNegativeClick()
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        )
    ) {

        BindingDialogContent(
            state = state,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun BindingDialogContent(
    state: BindingDialogState,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
        ) {
            Text(
                stringResource(id = R.string.binding),
                style = MaterialTheme.typography.h5
            )

            Spacer(Modifier.height(16.dp))

            JobFlowRadioButton(
                isSelected = state.binding == Binding.TYPE_SADDLE_STITCH,
                title = stringResource(id = R.string.saddle_stitched),
                onClick = {
                    state.binding = Binding.TYPE_SADDLE_STITCH
                }
            )

            Spacer(Modifier.height(8.dp))

            JobFlowRadioButton(
                isSelected = state.binding == Binding.TYPE_PERFECT,
                title = stringResource(id = R.string.perfect_binding),
                onClick = {
                    state.binding = Binding.TYPE_PERFECT
                }
            )

            Spacer(Modifier.height(8.dp))

            JobFlowRadioButton(
                isSelected = state.binding == Binding.TYPE_CASE,
                title = stringResource(id = R.string.case_binding),
                onClick = {
                    state.binding = Binding.TYPE_CASE
                }
            )

            Spacer(Modifier.height(16.dp))

            JobFlowTextField(
                value = state.remarks,
                onValueChange = {
                    if (!it.text.contains("\t"))
                        state.remarks = it
                },
                onTabPressed = {  },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                label = stringResource(id = R.string.remarks),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.hasFocus)
                            state.remarks = state.remarks.copy(
                                selection = TextRange(
                                    0,
                                    state.remarks.text.length
                                )
                            )
                    }
            )

            Spacer(Modifier.height(24.dp))

            DialogButtons(
                positiveButtonText = stringResource(id = R.string.save).toUpperCase(Locale.current),
                negativeButtonText = stringResource(id = R.string.cancel).toUpperCase(Locale.current),
                onPositiveClick = {
                    focusManager.clearFocus()
                    onPositiveClick()
                },
                onNegativeClick = {
                    focusManager.clearFocus()
                    onNegativeClick()
                })
        }
    }
}

private fun validateLaminationMicron(micron: String): Boolean {
    val converted = try {
        micron.toInt()
    } catch (e: Exception) {
        -1
    }
    return converted in 1..100
}

@Composable
fun LaminationMaterials(
    laminationDialogState: LaminationDialogState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        JobFlowRadioButton(
            isSelected = laminationDialogState.material == Lamination.MATERIAL_PVC,
            title = stringResource(id = R.string.pvc),
            onClick = { laminationDialogState.material = Lamination.MATERIAL_PVC }
        )

        JobFlowRadioButton(
            isSelected = laminationDialogState.material == Lamination.MATERIAL_BOPP,
            title = stringResource(id = R.string.bopp),
            onClick = { laminationDialogState.material = Lamination.MATERIAL_BOPP }
        )

        JobFlowRadioButton(
            isSelected = laminationDialogState.material == Lamination.MATERIAL_MATT,
            title = stringResource(id = R.string.matt),
            onClick = { laminationDialogState.material = Lamination.MATERIAL_MATT }
        )
    }
}

@Composable
fun DialogButtons(
    positiveButtonText: String,
    negativeButtonText: String,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    positiveButtonEnabled: () -> Boolean = { true }
) {

    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                focusManager.clearFocus()
                onNegativeClick()
            }
        ) {
            Text(negativeButtonText.toUpperCase(Locale.current))
        }
        Spacer(Modifier.width(24.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                onPositiveClick()
            },
            enabled = positiveButtonEnabled()
        ) {
            Text(positiveButtonText.toUpperCase(Locale.current))
        }
    }

}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Preview
@Composable
fun LaminationDialogPreview() {

    val state = remember { LaminationDialogState() }

    JobFlowTheme {
        LaminationDialogContent(
            state = state,
            onPositiveClick = { },
            onNegativeClick = {}
        )
    }

}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Preview
@Composable
fun BindingDialogPreview() {

    val state = remember { BindingDialogState() }

    JobFlowTheme {
        BindingDialogContent(
            state = state,
            onPositiveClick = { },
            onNegativeClick = {}
        )
    }

}
