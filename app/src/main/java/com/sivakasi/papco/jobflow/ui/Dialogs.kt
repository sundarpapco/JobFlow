package com.sivakasi.papco.jobflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun TextInputDialog(
    dialogState: TextInputDialogState<*>,
    onPositiveClick: (String) -> Unit = {},
    onNegativeClick: () -> Unit = {},
    dismissOnClickOutside: Boolean = true,
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

@ExperimentalFoundationApi
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
            Text(dialogState.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            SelectableTextField(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogFrame(
    onCancel: () -> Unit,
    content: @Composable ()->Unit
) {
    BasicAlertDialog(
        onDismissRequest = onCancel
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    title: String?,
    text: String,
    confirmationText: String,
    cancellationText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {

    DialogFrame(
        onCancel = onCancel
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            title?.let{
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = onCancel,
                ) {
                    Text(
                        text = cancellationText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                TextButton(
                    onClick = onConfirm,
                ) {
                    Text(
                        text = confirmationText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFlowAlertDialog(
    message: String,
    positiveButtonText: String,
    onPositiveClick: () -> Unit,
    negativeButtonText: String,
    title: String? = null,
    onDismissListener: () -> Unit = {}
) {

    AlertDialog(
      title = title,
        text = message,
        confirmationText = positiveButtonText,
        cancellationText = negativeButtonText,
        onConfirm = onPositiveClick,
        onCancel = onDismissListener
    )
}

@Preview
@Composable
private fun AlertDialogPreview() {
    JobFlowMaterial3Theme {
        JobFlowAlertDialog(
            message = "Are you sure want to delete this machine?",
            positiveButtonText = "Yes",
            negativeButtonText = "Cancel",
            onPositiveClick = { })
    }
}


@ExperimentalFoundationApi
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
    JobFlowMaterial3Theme {

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

    JobFlowMaterial3Theme {
        WaitDialog()
    }
}*/


