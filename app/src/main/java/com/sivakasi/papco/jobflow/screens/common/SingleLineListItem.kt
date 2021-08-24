package com.sivakasi.papco.jobflow.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.models.ClientUIModel
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@Composable
fun <T> SingleLineListItem(
    data: T,
    textBlock: (T) -> AnnotatedString,
    onClick: (T) -> Unit
) {

    Surface(
        color=MaterialTheme.colors.background,
        modifier = Modifier
            .requiredHeight(56.dp)
            .clickable { onClick(data) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = textBlock(data),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.fillMaxWidth(),
                overflow = TextOverflow.Ellipsis
            )
        }

    }

}


@Preview
@Composable
private fun PreviewSingleLineListItem() {

    val annotatedString = with(AnnotatedString.Builder("Suri graphix")) {
        addStyle(SpanStyle(MaterialTheme.colors.primary), 0, 3)
        toAnnotatedString()
    }
    val listData = ClientUIModel(1, annotatedString)
    JobFlowTheme {
        SingleLineListItem(
            data = listData,
            textBlock = { it.name },
            {}
        )
    }

}
