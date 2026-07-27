package com.sivakasi.papco.jobflow.screens.processinghistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.ProcessingHistory
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.screens.common.ErrorScreen
import com.sivakasi.papco.jobflow.screens.common.SearchListItem
import com.sivakasi.papco.jobflow.screens.common.fakeSearchModel
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
fun EntryProviderScope<NavKey>.previousHistoryScreenEntry(
    backStack: NavBackStack<NavKey>
){
    entry<AppGraph.PreviousHistory> {key->

        val viewModel: PreviousProcessingHistoryVM = hiltViewModel()

        PreviousHistoryScreen(
            viewModel = viewModel,
            onClose = {backStack.removeLastOrNull()}
        )

        LaunchedEffect(Unit) {
            viewModel.loadPreviousHistoryOfPlateNumber(key.plateNumber)
        }

    }
}


@Composable
private fun HistoryCircle(
    modifier: Modifier = Modifier,
    diameter: Dp
) {
    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .background(MaterialTheme.colors.primary)
    )
}

@Composable
private fun HistoryIndicator(
    modifier: Modifier = Modifier,
    circleDiameter: Dp,
    shouldDrawLine: Boolean = true
) {
    Column(
        modifier = modifier
            .width(circleDiameter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HistoryCircle(
            diameter = circleDiameter
        )

        if (shouldDrawLine)
            Divider(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .weight(2f),
                color = MaterialTheme.colors.primary
            )
        else
            Spacer(
                Modifier.fillMaxHeight()
            )
    }
}

@Composable
fun HistoryText(
    modifier: Modifier = Modifier,
    machineName: String,
    timeStamp: String
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = machineName,
            style = MaterialTheme.typography.subtitle1,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = timeStamp,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProcessingHistory(
    processingHistory: ProcessingHistory,
    shouldDrawLine: Boolean = true
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        HistoryIndicator(
            circleDiameter = 20.dp,
            shouldDrawLine = shouldDrawLine
        )

        Spacer(
            modifier = Modifier
                .width(32.dp)
        )

        HistoryText(
            Modifier.weight(2f),
            machineName = processingHistory.destinationName,
            timeStamp = processingHistory.timeStamp
        )
    }
}

@Composable
fun ProcessingHistoryList(
    modifier: Modifier = Modifier,
    items: List<ProcessingHistory>
) {
    if (items.isEmpty())
        return

    LazyColumn(
        modifier = modifier
    ) {
        items.forEachIndexed { index, element ->
            item(element.completionTime) {
                ProcessingHistory(
                    element,
                    index < items.size - 1
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun PreviousHistoryScreenContent(
    screenState: PreviousHistoryScreenState,
    onClick: (SearchModel) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SearchListItem(searchModel = screenState.printOrder, onClick = onClick)
            Spacer(Modifier.height(40.dp))
            ProcessingHistoryList(items = screenState.history)
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun PreviousHistoryScreen(
    viewModel: PreviousProcessingHistoryVM,
    onClose:()->Unit
){
    Scaffold(
        topBar = {
            JobFlowTopBar(
                title = stringResource(R.string.previous_processing_history),
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {paddingValues ->
        when(val screenState = viewModel.screenToRender){

            is LoadingStatus.Loading ->{
                LoadingScreen(Modifier.padding(paddingValues))
            }

            is LoadingStatus.Success<*> ->{

                PreviousHistoryScreenContent(
                    modifier = Modifier.padding(paddingValues),
                    screenState = screenState.data as PreviousHistoryScreenState,
                    onClick = {}
                )
            }

            is LoadingStatus.Error ->{
                ErrorScreen(
                    modifier = Modifier.padding(paddingValues),
                    error = screenState.exception
                )
            }

            else -> {}
        }
    }
}


@Preview
@Composable
private fun PreviewProcessingHistoryItem() {

    val history = remember {
        ProcessingHistory(
            destinationName = "Completed",
            completionTime =1661599617081
        )
    }

    JobFlowTheme {
        ProcessingHistory(
            processingHistory = history,
            shouldDrawLine = true
        )
    }

}

@Preview
@Composable
private fun PreviewHistoryText() {
    JobFlowTheme {
        HistoryText(
            machineName = "PO Created",
            timeStamp = "28/04/2022, 08:45 AM"
        )
    }
}

/*@Preview
@Composable
private fun PreviewHistoryList() {

    val history = remember {
        fakeHistory()
    }

    JobFlowTheme {
        ProcessingHistoryList(
            modifier = Modifier.padding(16.dp),
            items = history
        )
    }

}*/

@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewPreviousHistoryScreenContent(){

    val context = LocalContext.current
    val screenState = remember{
        PreviousHistoryScreenState(
            fakeSearchModel(context),
            fakeHistory()
        )
    }

    JobFlowTheme {
        PreviousHistoryScreenContent(
            screenState,
            onClick = {}
        )
    }

}

private fun fakeHistory():List<ProcessingHistory> {

    val time = getCalendarInstance().timeInMillis

    return listOf(
        ProcessingHistory(
            destinationId = "Creation",
            destinationName = "PO Created",
            completionTime = time
        ),

        ProcessingHistory(
            destinationId = "D3000S5",
            destinationName = "D3000S5",
            completionTime = time
        ),

        ProcessingHistory(
            destinationId = "Lamination",
            destinationName = "Lamination",
            completionTime = time
        ),

        ProcessingHistory(
            destinationId = "Completed",
            destinationName = "Completed",
            completionTime = time
        )
    )

}