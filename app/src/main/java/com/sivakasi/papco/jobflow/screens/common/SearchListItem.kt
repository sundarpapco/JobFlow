package com.sivakasi.papco.jobflow.screens.common

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import com.sivakasi.papco.jobflow.models.SearchModel


@Composable
fun SearchListItem(
    searchModel: SearchModel,
    onClick: (SearchModel) -> Unit
) {

    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = { onClick(searchModel) },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row {
                Surface(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(0.dp, 0.dp, 15.dp, 15.dp)
                ) {
                    Text(
                        text = searchModel.poNumberAndDate(),
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = searchModel.rid(),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(12.dp, 4.dp, 12.dp, 4.dp)
                )



                Text(
                    text = searchModel.status(),
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
                    textAlign = TextAlign.End
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            ClientNameRow(
                clientName = searchModel.billingName,
                dispatchCount = searchModel.dispatchCount
            )

            Row {
                Text(
                    text = searchModel.jobName,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )

                Text(
                    text = searchModel.colors,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }

            Text(
                text = searchModel.paperDetails,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                fontSize = 12.sp
            )

        }
    }
}

@Composable
fun ClientNameRow(
    clientName: String,
    dispatchCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = clientName,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        if (dispatchCount > 0) {
            Spacer(Modifier.width(8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            PartDispatchIcon(dispatchCount = dispatchCount)
        }
    }
}

@Composable
fun PartDispatchIcon(
    dispatchCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            modifier = Modifier.align(Alignment.Top),
            text = dispatchCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(Modifier.width(2.dp))

        Icon(
            modifier = Modifier.size(12.dp),
            painter = painterResource(id = R.drawable.ic_forward_send),
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Partial Dispatch Icon"
        )
    }
}

@Preview
@Composable
private fun PreviewPartialDispatchIcon() {

    JobFlowMaterial3Theme {
        PartDispatchIcon(dispatchCount = 1)
    }
}


@Preview
@Composable
private fun SearchListItemPreview() {

    val context = LocalContext.current
    val searchModel = remember { fakeSearchModel(context) }

    JobFlowMaterial3Theme {
        SearchListItem(searchModel = searchModel, onClick = {})
    }

}

fun fakeSearchModel(context: Context): SearchModel {

    val searchModel = SearchModel(context)
    return searchModel.apply {
        printOrderNumber = 18531
        printOrderDate = "12/05/2021"
        billingName = "Suri Graphix, Sivakasi"
        jobName = "Naiduhall Boxes 6V"
        plateNumber = 12022
        paperDetails = "58.5 x 91 Cms 100 GSM Real art paper - 5200 Sheets"
        invoiceNumber = "B/52"
        colors = "5 (CMYK+LT)"
        creationTime = getCalendarInstance().timeInMillis
        destinationId = DatabaseContract.DOCUMENT_DEST_COMPLETED
        dispatchCount = 3
    }

}