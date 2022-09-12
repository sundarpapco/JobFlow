package com.sivakasi.papco.jobflow.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException

@Composable
fun ErrorScreen(error:Throwable){

    Surface(
        modifier=Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Error Icon",
                    tint = MaterialTheme.colors.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = error.message ?: stringResource(id = R.string.error_unknown_error),
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )

            }
        }
    }

}

@Preview
@Composable
private fun PreviewErrorScreen(){

    val error = remember{
        ResourceNotFoundException("PO Not found")
    }

    JobFlowTheme {
        ErrorScreen(error = error)
    }

}