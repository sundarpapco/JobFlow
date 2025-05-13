package com.sivakasi.papco.jobflow.screens.destination

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.Duration
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun ReorderableCollectionItemScope.JobListItem(
    printOrder: PrintOrderUIModel,
    selected: Boolean,
    dragging: Boolean,
    dragEnabled: Boolean,
    onDragCompleted: () -> Unit,
    onClick: (PrintOrderUIModel) -> Unit,
    onLongClick: (PrintOrderUIModel) -> Unit,
    onPendingIconClicked:()->Unit
) {

    Card(
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(15.dp),
        border =
            if (selected || dragging)
                BorderStroke(3.dp, MaterialTheme.colors.primary)
            else
                BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant),
        onClick = { onClick(printOrder) },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (selected || dragging)
                        MaterialTheme.colors.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colors.background,
                )
                .combinedClickable(
                    onLongClick = { onLongClick(printOrder) },
                    onClick = { onClick(printOrder) }
                )
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Row {
                Surface(
                    color = if (selected || dragging)
                        MaterialTheme.colors.primary
                    else
                        MaterialTheme.colors.secondaryVariant,
                    shape = RoundedCornerShape(0.dp, 0.dp, 15.dp, 15.dp)
                ) {
                    Text(
                        text = printOrder.poNumberAndAge,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (printOrder.emergency)
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Urgent",
                        tint = MaterialTheme.colors.secondary
                    )

                if (printOrder.pendingReason.isNotEmpty())
                    Icon(
                        modifier = Modifier.clickable { onPendingIconClicked() }.padding(4.dp),
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Urgent",
                        tint = MaterialTheme.colors.error
                    )

                if (printOrder.partialDispatchCount > 0)
                    PartDispatchIcon(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically),
                        dispatchCount = printOrder.partialDispatchCount
                    )

                Spacer(Modifier.weight(1f))

                if (printOrder.isReprint)
                    Text(
                        text = stringResource(R.string.reprint),
                        color = MaterialTheme.colors.secondary,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .align(Alignment.CenterVertically)
                    )

                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Drag Handle",
                    tint = MaterialTheme.colors.secondaryVariant,
                    modifier = Modifier
                        .draggableHandle(enabled = dragEnabled, onDragStopped = onDragCompleted)
                        .padding(vertical = 4.dp)
                )
            }

            Row {
                Text(
                    text = printOrder.billingName,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = printOrder.runningTime.timeFormatString(),
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.caption
                )

            }

            Row {
                Text(
                    text = printOrder.jobName,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 12.sp
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = printOrder.colors,
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.caption,
                    maxLines = 1
                )
            }


            Text(
                text = printOrder.printingSizePaperDetail,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.caption,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                fontSize = 12.sp
            )

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
            text = dispatchCount.toString(),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )

        Spacer(Modifier.width(2.dp))

        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(id = R.drawable.ic_forward_send),
            tint = MaterialTheme.colors.secondary,
            contentDescription = "Partial Dispatch Icon"
        )
    }
}

@Preview
@Composable
private fun PreviewPartialDispatchIcon() {

    JobFlowTheme {
        PartDispatchIcon(dispatchCount = 1)
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun SearchListItemPreview() {

    val printOrderModel = remember { mockModel() }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { _, _ ->

    }

    JobFlowTheme {
        LazyColumn(
            state = lazyListState
        ) {
            item {
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = "1"
                ) {
                    JobListItem(
                        printOrder = printOrderModel,
                        selected = true,
                        dragging = false,
                        dragEnabled = false,
                        onDragCompleted = {},
                        onClick = {},
                        onLongClick = {},
                        onPendingIconClicked = {}
                    )
                }
            }
        }

    }
}


private fun mockModel(): PrintOrderUIModel {

    return PrintOrderUIModel().apply {
        printOrderNumber = 11548
        billingName = "Suri Graphix, Sivakasi"
        jobName = "NaiduHall Brochure"
        emergency = true
        isReprint = true
        poNumberAndAge = "11548 - 10 Days ago"
        printingSizePaperDetail = "58.5 x 91 Cms 100 GSM Real art paper - 5200 Sheets"
        runningTime = Duration(1, 30)
        colors = "CMYK+LB"
        hasSpotColors = true
        clientId = 1
        partialDispatchCount = 1
        pendingReason = "Something"
    }
}