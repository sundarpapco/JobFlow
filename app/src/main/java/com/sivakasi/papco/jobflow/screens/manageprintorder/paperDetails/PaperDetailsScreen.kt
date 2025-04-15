package com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails

import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.algolia.search.model.response.ResponseLogs
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.extensions.toast
import com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails.toPrintingDetails
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar


@Composable
private fun PaperDetailsScreen(
    screenState: PaperDetailsScreenState,
    onClose:()->Unit,
    onNext:()->Unit
){
    val context = LocalContext.current
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = if (screenState.isEditMode)
                    stringResource(R.string.create_job)
                else
                    stringResource(R.string.edit_job),
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 0.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        if(screenState.validate())
                            onNext()
                        else
                            context.toast(
                                context.getString(R.string.error_at_least_one_paper_detail_required)
                            )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.next)
                    )
                }
            }
        }
    ) {paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(top=24.dp, bottom = 0.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item(key = "Screen Heading") {
                Text(
                    text = stringResource(R.string.paper_details),
                    style = MaterialTheme.typography.h4
                )
            }

            item(key="spacer"){
                Spacer(Modifier.height(4.dp))
            }

            itemsIndexed(
                items = screenState.paperDetails
            ){index,item->
                PaperDetailListItem(
                    modifier = Modifier.animateItem(),
                    paperDetail = item,
                    onClick = {screenState.showPaperDetailDialog(index,it)},
                    onClose = {screenState.removePaper(index)}
                )
            }

            item(key="Add Button"){
                AddPaperDetailButton(
                    modifier = Modifier.animateItem(),
                    onClick = {
                        screenState.showPaperDetailDialog(
                            index = -1,
                            paperDetail = PaperDetail()
                        )
                    }
                )
            }

        }

    }

    screenState.paperDialogState?.let{
        PaperDetailDialog(
          state = it,
            onDismiss = {screenState.hidePaperDetailDialog()},
            onSave = {index,paperDetail->
                screenState.addPaper(index,paperDetail)
                screenState.hidePaperDetailDialog()
            }
        )
    }
}

@Composable
private fun AddPaperDetailButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add paper Detail"
        )

        Text(
            text = stringResource(R.string.add_paper_detail),
            color = MaterialTheme.colors.primary
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PaperDetailListItem(
    paperDetail: PaperDetail,
    onClick: (PaperDetail) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.background,
        onClick = {
            onClick(paperDetail)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (paperDetail.partyPaper)
                        stringResource(R.string.party_own)
                    else
                        stringResource(R.string.our_own),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    modifier = Modifier
                        .clickable{
                            onClose()
                        },
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.primary
                )

            }

            Text(
                text = paperDetail.toString(),
                style = MaterialTheme.typography.body2
            )
        }
    }
}

/*@Preview(name = "List Item")
@Composable
private fun PreviewListItem() {

    val paperDetail = remember {
        PaperDetail(
            height = 58.5f,
            width = 91f,
            gsm = 100,
            name = "Real Art Paper",
            sheets = 5000
        )
    }

    JobFlowTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
            PaperDetailListItem(
                paperDetail = paperDetail,
                onClick = {},
                onClose = {}
            )
        }
    }

}

@Preview(name = "Paper Detail Button")
@Composable
private fun PreviewAddPaperDetailButton() {

    JobFlowTheme {
        Surface(
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
           AddPaperDetailButton {
               Log.d("SAATVIK","Add paper detail Clicked")
           }
        }
    }

}*/

@Preview(name = "Paper Detail Screen")
@Composable
private fun PreviewAddPaperDetailScreen() {

    var context = LocalContext.current

    val state by remember {
        val state = PaperDetailsScreenState(context,false)
        state.paperDetails = emptyList()
        mutableStateOf(state)
    }

    JobFlowTheme {
       PaperDetailsScreen(
           screenState = state,
           onClose = {},
           onNext = {}
       )
    }

}