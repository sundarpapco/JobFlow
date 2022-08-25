package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sivakasi.papco.jobflow.R

class TextInputDialogState<T>(
    val positiveButtonText: String,
    val negativeButtonText: String? = null
) {

    var title: String = ""
    var text by mutableStateOf(TextFieldValue(""))
    var isProcessing by mutableStateOf(false)
    var data: T? = null
    var error: String? by mutableStateOf(null)
    var label: String = "Text"

}

@ExperimentalComposeUiApi
@Composable
fun TextInputDialog(
    dialogState: TextInputDialogState<*>,
    onPositiveClick: (String) -> Unit = {},
    onNegativeClick: () -> Unit = {},
    dismissOnClickOutside: Boolean = false,
    allowBlank: Boolean = false
) {

    Dialog(
        onDismissRequest = {
            //Allow closing of the dialog when no processing in in progress
            if (!dialogState.isProcessing) {
                onNegativeClick()
            }

        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        TextInputDialogContent(
            dialogState = dialogState,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick,
            allowBlank = allowBlank
        )
    }

}


@ExperimentalComposeUiApi
@Composable
private fun TextInputDialogContent(
    dialogState: TextInputDialogState<*>,
    onPositiveClick: (String) -> Unit,
    onNegativeClick: () -> Unit,
    allowBlank: Boolean = false

) {

    val textFieldFocus = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(dialogState.title, style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(8.dp))
            SelectableTextField(
                onTabPressed = {},
                value = dialogState.text,
                onValueChange = {
                    dialogState.error = null;dialogState.text = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(textFieldFocus),
                singleLine = true,
                label = dialogState.label,
                readOnly = dialogState.isProcessing,
                error = dialogState.error
            )

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (dialogState.isProcessing)
                        CircularProgressIndicator()
                }

                //Render the Negative button only when the negative Button text is set
                dialogState.negativeButtonText?.let {
                    Button(
                        onClick = {
                            textFieldFocus.freeFocus()
                            keyboardController?.hide()
                            onNegativeClick()
                        },
                        enabled = !dialogState.isProcessing
                    ) {
                        Text(it)
                    }

                    Spacer(Modifier.width(24.dp))
                }

                Button(
                    onClick = {
                        focusManager.clearFocus(true)
                        onPositiveClick(dialogState.text.text)
                    },
                    enabled = if (!dialogState.isProcessing) {
                        if (allowBlank)
                            true
                        else
                            dialogState.text.text.isNotBlank()
                    } else
                        false
                ) {
                    Text(dialogState.positiveButtonText)
                }
            }
        }

    }


    DisposableEffect(Unit) {
        textFieldFocus.requestFocus()
        onDispose { }
    }
}


@Composable
fun WaitDialog(msg: String = "") {

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.requiredSize(40.dp)
                )

                Spacer(Modifier.width(24.dp))

                Text(
                    modifier = Modifier.weight(1f),
                    text = msg.ifBlank { stringResource(id = R.string.one_moment_please) }
                )
            }
        }
    }
}

@Composable
fun JobFlowAlertDialog(
    message: String,
    positiveButtonText: String,
    onPositiveClick: () -> Unit,
    negativeButtonText: String? = null,
    title: String? = null,
    onNegativeClick: () -> Unit = {},
    onDismissListener: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissListener,
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {

                negativeButtonText?.let {
                    TextButton(onClick = onNegativeClick) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.button
                        )
                    }

                    Spacer(Modifier.width(16.dp))
                }

                TextButton(onClick = onPositiveClick) {
                    Text(
                        text = positiveButtonText,
                        style = MaterialTheme.typography.button
                    )
                }
            }
        },
        title = title?.let {
            {
                Text(
                    text = it,
                    style = LocalTextStyle.current
                )
            }
        },
        text = {
            Text(
                text = message,
                style = LocalTextStyle.current
            )
        }
    )
}

@Preview
@Composable
private fun AlertDialogPreview() {
    JobFlowTheme {
        JobFlowAlertDialog(
            message = "Are you sure want to delete this machine?",
            positiveButtonText = "Yes",
            onPositiveClick = { })
    }
}


@ExperimentalComposeUiApi
@Preview(
    name = "TextDialogContent"
)
@Composable
private fun TextInputDialogPreview() {

    val dialogState = remember {
        TextInputDialogState<Unit>("OK", "CANCEL").apply {
            title = "Title"
            label = "Label"
        }
    }
    JobFlowTheme {

        TextInputDialogContent(
            dialogState = dialogState,
            onPositiveClick = {},
            onNegativeClick = {}
        )
    }

}

/*@Preview
@Composable
private fun PreviewWaitDialog() {

    JobFlowTheme {
        WaitDialog()
    }
}*/


