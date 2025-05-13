package com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.extensions.isPositiveFloatNumber
import com.sivakasi.papco.jobflow.extensions.isPositiveNumber
import com.sivakasi.papco.jobflow.ui.JobFlowRadioButton
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme


@Composable
fun PaperDetailDialog(
    state: PaperDetailDialogState,
    onDismiss: () -> Unit,
    onSave: (Int, PaperDetail) -> Unit
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface {
            PaperDetailDialogContent(
                dialogState = state,
                onSave = onSave
            )
        }

    }

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun PaperDetailDialogContent(
    dialogState: PaperDetailDialogState,
    onSave: (Int, PaperDetail) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        val heightFocus = remember { FocusRequester() }
        Text(
            text = stringResource(R.string.paper_details),
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            JobFlowRadioButton(
                isSelected = dialogState.isPartyPaper,
                onClick = {
                    dialogState.isPartyPaper = true
                },
                title = stringResource(R.string.party_own)
            )

            Spacer(Modifier.width(24.dp))

            JobFlowRadioButton(
                isSelected = !dialogState.isPartyPaper,
                onClick = {
                    dialogState.isPartyPaper = false
                },
                title = stringResource(R.string.our_own)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(heightFocus)
                    .onFocusChanged {
                        if (it.hasFocus)
                            dialogState.height = dialogState.height.copy(
                                selection = TextRange(0, dialogState.height.text.length)
                            )
                    },
                value = dialogState.height,
                onValueChange = {
                    dialogState.heightError = null
                    if (it.text.isPositiveFloatNumber()) { // Ensures only positive floats
                        dialogState.height = it
                    }
                },
                label = stringResource(R.string.height),
                singleLine = true,
                error = dialogState.heightError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus)
                            dialogState.width = dialogState.width.copy(
                                selection = TextRange(0, dialogState.width.text.length)
                            )
                    },
                value = dialogState.width,
                onValueChange = {
                    dialogState.widthError = null
                    if (it.text.isPositiveFloatNumber()) {
                        dialogState.width = it
                    }
                },
                label = stringResource(R.string.width),
                singleLine = true,
                error = dialogState.widthError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus)
                            dialogState.gsm = dialogState.gsm.copy(
                                selection = TextRange(0, dialogState.gsm.text.length)
                            )
                    },
                value = dialogState.gsm,
                onValueChange = {
                    dialogState.gsmError = null
                    if(it.text.isPositiveNumber())
                        dialogState.gsm = it
                },
                label = stringResource(R.string.gsm),
                singleLine = true,
                error = dialogState.gsmError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        JobFlowTextField(
            modifier = Modifier.onFocusChanged {
                if (it.hasFocus)
                    dialogState.substrate = dialogState.substrate.copy(
                        selection = TextRange(0, dialogState.substrate.text.length)
                    )
            },
            value = dialogState.substrate,
            onValueChange = {
                dialogState.substrateError = null
                dialogState.substrate = it
            },
            label = stringResource(R.string.substrate),
            singleLine = true,
            error = dialogState.substrateError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (!it.hasFocus) {
                            dialogState.evaluateExpression()
                        } else
                            dialogState.expression = dialogState.expression.copy(
                                selection = TextRange(0, dialogState.expression.text.length)
                            )
                    },
                value = dialogState.expression,
                onValueChange = {
                    dialogState.expression = it
                },
                label = stringResource(R.string.expression),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            JobFlowTextField(
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus)
                            dialogState.sheets = dialogState.sheets.copy(
                                selection = TextRange(0, dialogState.sheets.text.length)
                            )
                    },
                value = dialogState.sheets,
                onValueChange = {
                    dialogState.sheetsError = null
                    if(it.text.isPositiveNumber())
                        dialogState.sheets = it
                },
                label = stringResource(R.string.sheets),
                singleLine = true,
                error = dialogState.sheetsError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = {
                    if (dialogState.validate())
                        onSave(dialogState.editingIndex, dialogState.toPaperDetail())
                }
            ) {
                Text(
                    text = stringResource(R.string.save)
                )
            }
        }

        LaunchedEffect(Unit) {
            heightFocus.requestFocus()
        }

    }
}

@Preview
@Composable
private fun ContentPreview() {

    val context = LocalContext.current

    val state = remember {
        PaperDetailDialogState(context, 0, PaperDetail())
    }

    JobFlowTheme {
        PaperDetailDialog(
            state = state,
            onDismiss = {

            },
            onSave = { _, _ ->

            }
        )
    }
}

