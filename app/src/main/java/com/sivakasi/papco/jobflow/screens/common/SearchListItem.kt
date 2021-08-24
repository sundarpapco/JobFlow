package com.sivakasi.papco.jobflow.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.getCalendarInstance
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

@ExperimentalMaterialApi
@Composable
fun SearchListItem(
    searchModel: SearchModel,
    onClick: (SearchModel) -> Unit
) {

    Card(
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant),
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
                    color = MaterialTheme.colors.secondaryVariant,
                    shape = RoundedCornerShape(0.dp, 0.dp, 15.dp, 15.dp)
                ) {
                    Text(
                        text = searchModel.poNumberAndDate(),
                        color = MaterialTheme.colors.onSecondary,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = searchModel.rid(),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp, 4.dp, 12.dp, 4.dp)
                )

                Text(
                    text = searchModel.status(),
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(4.dp)
                )

            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = searchModel.billingName,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Row {
                Text(
                    text = searchModel.jobName,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )

                Text(
                    text = searchModel.colors,
                    color = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.caption,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = searchModel.paperDetails,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                fontSize = 12.sp
            )

        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun SearchListItemPreview() {

    val searchModel = SearchModel(LocalContext.current)
    searchModel.apply {
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
    }

    JobFlowTheme {
        SearchListItem(searchModel = searchModel, onClick = {})
    }

}