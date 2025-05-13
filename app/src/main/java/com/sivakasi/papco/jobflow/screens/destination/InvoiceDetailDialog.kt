package com.sivakasi.papco.jobflow.screens.destination

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTextField
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun InvoiceDetailDialog(
    onPositiveClick: (String,Boolean) -> Unit,
    onDismiss:()->Unit
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        InvoiceDialogContent(
            onPositiveClick = onPositiveClick
        )
    }

}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
private fun InvoiceDialogContent(
    onPositiveClick: (String,Boolean) -> Unit
) {

    val textFieldFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var invoiceNumber by rememberSaveable{
        mutableStateOf("")
    }

    var partialDispatch by rememberSaveable { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                stringResource(R.string.invoice_detail),
                style = MaterialTheme.typography.h5
            )
            Spacer(Modifier.height(8.dp))
            JobFlowTextField(
                value = invoiceNumber,
                onValueChange = {
                    invoiceNumber=it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(textFieldFocus),
                singleLine = true,
                label = stringResource(R.string.invoice_number),
                readOnly = false
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = partialDispatch,
                    onCheckedChange = {partialDispatch=it}
                )

                Text(
                    text= stringResource(R.string.partial_dispatch),
                    style = MaterialTheme.typography.caption
                )

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        focusManager.clearFocus(true)
                        onPositiveClick(invoiceNumber.trim(),partialDispatch)
                    },
                    enabled = invoiceNumber.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }

    }


    DisposableEffect(Unit) {
        textFieldFocus.requestFocus()
        onDispose { }
    }
}


@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Preview(
    name = "TextDialogContent"
)
@Composable
private fun InvoiceDialogContentPreview() {

    JobFlowTheme {

        InvoiceDetailDialog(
            onPositiveClick = {_,_->},
            onDismiss = {}
        )
    }

}
