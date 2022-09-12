package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.print.PrintOrderAdapter
import com.sivakasi.papco.jobflow.print.PrintOrderReport
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.screens.common.ErrorScreen
import com.sivakasi.papco.jobflow.screens.manageprintorder.FragmentAddPO
import com.sivakasi.papco.jobflow.screens.processinghistory.PreviousHistoryFragment
import com.sivakasi.papco.jobflow.screens.processinghistory.ProcessingHistoryList
import com.sivakasi.papco.jobflow.ui.*
import kotlinx.coroutines.*

val LocalNavigation = compositionLocalOf<NavController> { error("Navigation must be initialized") }

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
val LocalViewModel =
    compositionLocalOf<ComposeViewModelFragmentVM> { error("ViewModel must be initialized") }

@ExperimentalMaterialApi
val LocalScreenState =
    compositionLocalOf<ViewPrintOrderScreenState> { error("Screen State must be initialized") }
val LocalActivityContext =
    compositionLocalOf<Context> { error("Activity Context must be initialized") }

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalCoroutinesApi
@Composable
fun ViewPrintOrderScreen(
    viewModel: ComposeViewModelFragmentVM,
    navController: NavController,
    activityContext: Context
) {

    CompositionLocalProvider(
        LocalNavigation provides navController,
        LocalViewModel provides viewModel,
        LocalScreenState provides viewModel.screenState,
        LocalActivityContext provides activityContext
    ) {

        JobFlowTheme {

            val screenState = LocalScreenState.current
            val scope = rememberCoroutineScope()

            BackHandler {
                if (screenState.modalBottomSheetState.isVisible) {
                    scope.launch {
                        screenState.modalBottomSheetState.hide()
                    }
                } else
                    navController.popBackStack()
            }

            ModalBottomSheetLayout(
                sheetContent = { ViewPrintOrderBottomSheet() },
                sheetState = screenState.modalBottomSheetState,
                sheetShape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                scrimColor = MaterialTheme.colors.background.copy(alpha = 0.6f)
            ) {
                Scaffold(
                    topBar = { ViewPrintOrderTopBar() },
                    floatingActionButton = {
                        if (screenState.fabShowing)
                            FloatingActionButton(
                                backgroundColor = MaterialTheme.colors.primary,
                                onClick = {
                                    navigateToEditPrintOrderScreen(navController, screenState)
                                }
                            ) {
                                Icon(Icons.Filled.Edit, "Edit Print Order Button")
                            }
                    }
                ) {
                    PrintOrderScreenContent(screenState = screenState)
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun ViewPrintOrderBottomSheet() {
    val screenState = LocalScreenState.current
    val sheetState = screenState.modalBottomSheetState
    val printOrder = screenState.printOrder
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (printOrder == null) {
        Spacer(modifier = Modifier.height(20.dp))
        return
    }

    when (screenState.modalSheetContent) {

        ViewPrintOrderScreenState.ModalSheetContent.NONE -> {
            Spacer(Modifier.height(20.dp))
        }

        ViewPrintOrderScreenState.ModalSheetContent.PROCESSING_HISTORY -> {
            ProcessingHistorySheet(
                history = printOrder.completeProcessingHistory(context),
                sheetState = sheetState
            )
        }

        ViewPrintOrderScreenState.ModalSheetContent.PART_DISPATCHES -> {
            if (printOrder.partialDispatches.isEmpty()) {
                Spacer(Modifier.height(20.dp))
                LaunchedEffect(key1 = printOrder) {
                    scope.launch {
                        sheetState.hide()
                    }
                }
            } else {
                PartDispatchesSheet(
                    dispatches = printOrder.partialDispatches,
                    sheetState = sheetState
                )
            }
        }

    }

}

@ExperimentalMaterialApi
@Composable
fun PrintOrderScreenContent(
    screenState: ViewPrintOrderScreenState
) {

    val navController = LocalNavigation.current

    if (screenState.isLoading) {
        LoadingScreen()
        return
    }

    screenState.error?.let {
        ErrorScreen(error = Exception(it))
        return
    }

    screenState.printOrderRenderInfo?.let {
        PrintOrder(printOrder = it)
    }

    if (screenState.isWaiting) {
        WaitDialog()
    }

    if (screenState.poMoved) {
        JobFlowAlertDialog(
            title = stringResource(id = R.string.po_not_found),
            message = stringResource(id = R.string.po_not_found_desc),
            positiveButtonText = stringResource(id = R.string.exit).toUpperCase(Locale.current),
            onPositiveClick = { navController.popBackStack() }
        )
    }

}


@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalCoroutinesApi
@Composable
private fun ViewPrintOrderTopBar() {

    val activityContext = LocalActivityContext.current
    val screenState = LocalScreenState.current
    val navController = LocalNavigation.current
    val viewModel = LocalViewModel.current
    val scope = rememberCoroutineScope()
    val menuItems = screenState.menuItems

    JobFlowTopBar(
        title = screenState.printOrderRenderInfo?.title ?: "",
        subtitle = screenState.destinationName,
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Filled.ArrowBack, null)
            }
        },
        actions = {
            menuItems?.let {
                OptionsMenu(
                    menuItems = it,
                    onItemClick = { itemId ->
                        onOptionsItemSelected(
                            activityContext, itemId, viewModel, navController, scope
                        )
                    }
                )
            }
        }
    )
}


@Composable
fun PrintOrder(
    printOrder: PrintOrderRenderInfo
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(start = 16.dp, end = 16.dp)
    ) {

        item {
            Spacer(Modifier.height(16.dp))
        }

        item {
            PODetails(poDetails = printOrder.poDetailsRenderInfo)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            PlateMakingDetails(details = printOrder.plateMakingDetailsRenderInfo)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            PaperDetails(details = printOrder.paperDetailsRenderInfo)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            PrintingDetails(detail = printOrder.printingDetailRenderInfo)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            PostPressDetails(details = printOrder.postPressDetailRenderInfo)
        }
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }

    }
}

@Composable
fun PODetails(
    poDetails: PODetailsRenderInfo
) {
    Surface(
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            Modifier.padding(12.dp)
        ) {
            DetailRow(
                label = stringResource(id = R.string.date),
                detail = poDetails.date
            )

            DetailRow(
                label = stringResource(id = R.string.client_name),
                detail = poDetails.clientName
            )

            DetailRow(
                label = stringResource(id = R.string.job_name),
                detail = poDetails.jobName
            )

            DetailRow(
                label = stringResource(id = R.string.printing_size),
                detail = poDetails.printingSize
            )

            DetailRow(
                label = stringResource(id = R.string.printing_quantity),
                detail = poDetails.printingQuantity
            )

            DetailRow(
                label = stringResource(id = R.string.job_type),
                detail = poDetails.jobType
            )

            if (poDetails.invoice.isNotBlank()) {
                DetailRow(
                    label = stringResource(id = R.string.invoice),
                    detail = poDetails.invoice
                )
            }
        }

    }
}

@Composable
fun PlateMakingDetails(
    details: PlateMakingDetailsRenderInfo
) {

    Column {
        Text(
            text = stringResource(id = R.string.plate_making_details),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                Modifier.padding(12.dp)
            ) {
                DetailRow(
                    label = stringResource(id = R.string.plate_number_info),
                    detail = details.plateNumber
                )

                DetailRow(
                    label = stringResource(id = R.string.trimming_size),
                    detail = details.trimmingSize
                )

                DetailRow(
                    label = stringResource(id = R.string.job_size),
                    detail = details.jobSize
                )

                DetailRow(
                    label = stringResource(id = R.string.gripper),
                    detail = details.gripper
                )

                DetailRow(
                    label = stringResource(id = R.string.tail),
                    detail = details.tail
                )

                DetailRow(
                    label = stringResource(id = R.string.machine),
                    detail = details.machine
                )

                DetailRow(
                    label = stringResource(id = R.string.screen),
                    detail = details.screen
                )

                DetailRow(
                    label = stringResource(id = R.string.backside),
                    detail = details.backside
                )

                if (details.backsideMachine.isNotBlank())
                    DetailRow(
                        label = stringResource(id = R.string.backside_machine),
                        detail = details.backsideMachine
                    )
            }
        }

    }
}

@Composable
private fun PaperDetails(
    details: List<PaperDetailRenderInfo>
) {
    if (details.isEmpty())
        return

    Column {
        Text(
            text = stringResource(id = R.string.paper_details),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                Modifier.padding(12.dp)
            ) {


                details.forEachIndexed { index, detail ->

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        if (index > 0)
                            Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = detail.owner,
                                style = MaterialTheme.typography.body1
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                modifier = Modifier.weight(2f),
                                text = detail.sheets,
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.End
                            )
                        }

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = detail.paperDetail,
                            style = MaterialTheme.typography.body1
                        )

                        if (index < details.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                }
            }
        }

    }
}

@Composable
private fun PrintingDetails(
    detail: PrintingDetailsRenderInfo
) {
    Column {
        Text(
            text = stringResource(id = R.string.printing_details),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                Modifier.padding(12.dp)
            ) {
                Row {
                    Text(
                        text = stringResource(id = R.string.colours),
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        modifier = Modifier.weight(2f),
                        text = detail.colours,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.End
                    )
                }

                if (detail.printingInstructions.isNotBlank()) {

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = detail.printingInstructions,
                        style = MaterialTheme.typography.body1
                    )

                }
            }
        }

    }
}

@Composable
private fun PostPressDetails(
    details: List<PostPressDetailRenderInfo>
) {
    if (details.isEmpty())
        return

    Column {
        Text(
            text = stringResource(id = R.string.post_press_details),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.secondary
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(
                Modifier.padding(12.dp)
            ) {


                details.forEachIndexed { index, detail ->

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        if (index > 0)
                            Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = detail.name,
                                style = MaterialTheme.typography.body1
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                modifier = Modifier.weight(2f),
                                text = detail.details,
                                style = MaterialTheme.typography.body1,
                                textAlign = TextAlign.End
                            )
                        }

                        if (detail.remarks.isNotBlank())
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = detail.remarks,
                                style = MaterialTheme.typography.body2
                            )

                        if (index < details.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                }
            }
        }

    }

}

@ExperimentalMaterialApi
@Composable
private fun ProcessingHistorySheet(
    history: List<ProcessingHistory>,
    sheetState: ModalBottomSheetState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        Modifier.padding(top = 24.dp, start = 32.dp, end = 32.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(2f),
                text = context.getString(R.string.processing_history),
                style = MaterialTheme.typography.h5
            )
            IconButton(onClick = {
                scope.launch {
                    sheetState.hide()
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        ProcessingHistoryList(items = history)
        Spacer(Modifier.height(32.dp))
    }

}

@ExperimentalMaterialApi
@Composable
private fun PartDispatchesSheet(
    dispatches: List<PartialDispatch>,
    sheetState: ModalBottomSheetState
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        Modifier.padding(top = 24.dp, start = 32.dp, end = 32.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(2f),
                text = context.getString(R.string.partial_dispatches),
                style = MaterialTheme.typography.h5
            )
            IconButton(onClick = {
                scope.launch {
                    sheetState.hide()
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        PartialDispatchList(dispatches = dispatches)
        Spacer(Modifier.height(32.dp))
    }

}

@Composable
private fun PartialDispatchList(
    modifier: Modifier = Modifier,
    dispatches: List<PartialDispatch>
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(dispatches) { dispatch ->
            PartialDispatchListItem(dispatch = dispatch)
        }
    }
}


@Composable
fun DetailRow(
    label: String,
    detail: String
) {
    Row(
        Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.weight(2f),
            text = detail,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PartialDispatchListItem(
    dispatch: PartialDispatch
) {
    Row(
        modifier = Modifier.height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RupeeRound(Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))

        //Text(text = "SUNDARAVEL")
        TwoLineListItem(
            modifier = Modifier.weight(1f),
            title = dispatch.invoiceNumber,
            subtitle = calendarWithTime(dispatch.date).asReadableTimeStamp()
        )

    }
}

@Composable
private fun RupeeRound(
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.primary,
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.rupee_symbol),
                style = MaterialTheme.typography.h5
            )
        }
    }

}

@Preview
@Composable
private fun PreviewPartialDispatchList() {

    val time = remember { getCalendarInstance().timeInMillis }
    val dispatches = remember {
        listOf(
            PartialDispatch(
                "GST/15",
                time
            ),
            PartialDispatch(
                "GST/16",
                time
            ),
            PartialDispatch(
                "GST/17",
                time
            ),
            PartialDispatch(
                "GST/18",
                time
            ),
        )
    }

    JobFlowTheme {
        PartialDispatchList(dispatches = dispatches)
    }
}

@Preview
@Composable
private fun PreviewDispatchListItem() {
    val dispatch = remember {
        PartialDispatch(
            "GST/15",
            getCalendarInstance().timeInMillis
        )
    }

    JobFlowTheme {
        PartialDispatchListItem(dispatch = dispatch)
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewProcessingHistorySheet() {

    val time = remember { getCalendarInstance().timeInMillis }
    val history = remember {
        listOf(
            ProcessingHistory(
                destinationId = "PO Created",
                destinationName = "PO Created",
                completionTime = time
            ),
            ProcessingHistory(
                destinationId = "Completed",
                destinationName = "Completed",
                completionTime = time
            )
        )
    }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    JobFlowTheme {

        Surface {
            ProcessingHistorySheet(history = history, sheetState)
        }
    }

}

@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewPartialDispatchSheet() {

    val time = remember { getCalendarInstance().timeInMillis }
    val dispatches = remember {
        listOf(
            PartialDispatch(
                "GST/15",
                time
            ),
            PartialDispatch(
                "GST/16",
                time
            ),
            PartialDispatch(
                "GST/17",
                time
            ),
            PartialDispatch(
                "GST/18",
                time
            ),
        )
    }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    JobFlowTheme {

        Surface {
            PartDispatchesSheet(dispatches = dispatches, sheetState = sheetState)
        }
    }

}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalCoroutinesApi
private fun onOptionsItemSelected(
    activityContext: Context,
    id: String,
    viewModel: ComposeViewModelFragmentVM,
    navController: NavController,
    scope: CoroutineScope
) {

    val screenState = viewModel.screenState

    when (id) {

        activityContext.getString(R.string.print) -> {
            print(activityContext, screenState.printOrder!!, viewModel.printOrderReport)
        }

        activityContext.getString(R.string.menu_share_as_pdf) -> {
            sharePdfFile(activityContext, viewModel, scope)
        }

        activityContext.getString(R.string.notes) -> {
            navigateToNotesScreen(
                navController,
                screenState.destinationId!!,
                screenState.printOrder!!.documentId(),
                screenState.printOrder!!.notes
            )
        }

        activityContext.getString(R.string.repeat_this_job) -> {
            screenState.printOrder?.let {
                repeatThisJob(
                    navController, it.printOrderNumber
                )
            }
        }

        activityContext.getString(R.string.previous_processing_history) -> {
            screenState.printOrder?.let {
                navigateToPreviousHistoryScreen(
                    navController, it
                )
            }
        }

        activityContext.getString(R.string.processing_history) -> {
            screenState.modalSheetContent =
                ViewPrintOrderScreenState.ModalSheetContent.PROCESSING_HISTORY
            scope.launch {
                screenState.modalBottomSheetState.show()
            }
        }

        activityContext.getString(R.string.partial_dispatches) -> {
            screenState.modalSheetContent =
                ViewPrintOrderScreenState.ModalSheetContent.PART_DISPATCHES
            scope.launch {
                screenState.modalBottomSheetState.show()
            }
        }

    }
}

private fun print(
    activityContext: Context,
    printOrder: PrintOrder?,
    printOrderReport: PrintOrderReport
) {
    printOrder?.let {
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
        val printManager = activityContext.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "PrintOrder"
        val printAdapter = PrintOrderAdapter(it, printOrderReport)
        printManager.print(jobName, printAdapter, printAttributes)
    }
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
private fun sharePdfFile(
    activityContext: Context,
    viewModel: ComposeViewModelFragmentVM,
    scope: CoroutineScope
) {

    val screenState = viewModel.screenState
    val printOrderReport = viewModel.printOrderReport

    scope.launch(Dispatchers.IO) {
        try {
            screenState.isWaiting = true
            val fileName = printOrderReport.generatePdfFile(screenState.printOrder!!)
            screenState.isWaiting = false
            activityContext.shareReport(fileName)
        } catch (e: Exception) {
            screenState.toastError =
                e.message ?: activityContext.getString(R.string.error_unknown_error)
        }

    }

}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
private fun navigateToEditPrintOrderScreen(
    navController: NavController,
    screenState: ViewPrintOrderScreenState
) {
    screenState.printOrder?.let {
        screenState.destinationId?.let { destination ->
            navController.navigate(
                R.id.action_composeViewPrintOrderFragment_to_print_order_flow,
                FragmentAddPO.getArgumentBundle(it.printOrderNumber, destination)
            )
        }
    }
}

@ExperimentalCoroutinesApi
private fun navigateToNotesScreen(
    navController: NavController,
    destinationId: String,
    poId: String = "",
    notes: String
) {
    navController.navigate(
        R.id.action_composeViewPrintOrderFragment_to_notesFragment,
        NotesFragment.getArguments(destinationId, poId, notes)
    )
}

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@FlowPreview
private fun navigateToPreviousHistoryScreen(
    navController: NavController,
    printOrder: PrintOrder
) {
    navController.navigate(
        R.id.action_composeViewPrintOrderFragment_to_previousHistoryFragment,
        PreviousHistoryFragment.getArgumentBundle(printOrder.plateMakingDetail.plateNumber)
    )
}

@ExperimentalCoroutinesApi
private fun repeatThisJob(
    navController: NavController,
    poNumber: Int
) {
    navController.navigate(
        R.id.action_composeViewPrintOrderFragment_to_print_order_flow,
        FragmentAddPO.getArgumentBundle(
            poNumber,
            DatabaseContract.DOCUMENT_DEST_NEW_JOBS,
            true
        )
    )
}

/*@Preview
@Composable
fun PreviewPODetails() {

    val poDetails =
        remember {
            PODetailsRenderInfo().apply {
                date = "05/08/2022"
                clientName = "Sreenidhi Raja Packaging Pvt Ltd"
                jobName = "Amaron B20 FLO Sheets Proof"
                printingSize = "496 X 724 mm"
                printingQuantity = "400 Sheets"
                jobType = "New Job"
                invoice = "Self"
            }
        }

    JobFlowTheme {
        Surface(
            Modifier.fillMaxWidth()
        ) {
            PODetails(poDetails = poDetails)
        }
    }

}

@Preview
@Composable
fun PreviewPlateMakingDetails() {
    val context = LocalContext.current
    val plateMakingDetails =
        remember {
            PrintOrderRenderInfo.from(context, fakePrintOrder()).plateMakingDetailsRenderInfo
        }

    JobFlowTheme {
        Box(
            Modifier.background(MaterialTheme.colors.background)
        ) {
            Surface(
                Modifier.fillMaxWidth()
            ) {
                PlateMakingDetails(details = plateMakingDetails)
            }
        }
    }
}

@Preview
@Composable
fun PreviewPaperDetails(){

    val context = LocalContext.current
    val render =
        remember {
            PrintOrderRenderInfo.from(context, fakePrintOrder()).paperDetailsRenderInfo
        }

    JobFlowTheme {
        Box(
            Modifier.background(MaterialTheme.colors.background)
        ) {
            Surface(
                Modifier.fillMaxWidth()
            ) {
                PaperDetails(details = render)
            }
        }
    }

}

@Preview
@Composable
fun PreviewPrintingDetail(){

    val context = LocalContext.current
    val render =
        remember {
            PrintOrderRenderInfo.from(context, fakePrintOrder()).printingDetailRenderInfo
        }

    JobFlowTheme {
        Box(
            Modifier.background(MaterialTheme.colors.background)
        ) {
            Surface(
                Modifier.fillMaxWidth()
            ) {
                PrintingDetails(detail = render)
            }
        }
    }

}

@Preview
@Composable
fun PreviewPostPressDetails(){

    val context = LocalContext.current
    val render =
        remember {
            PrintOrderRenderInfo.from(context, fakePrintOrder()).postPressDetailRenderInfo
        }

    JobFlowTheme {
        Box(
            Modifier.background(MaterialTheme.colors.background)
        ) {
            Surface(
                Modifier.fillMaxWidth()
            ) {
                PostPressDetails(details = render)
            }
        }
    }

}*/

/*@Preview
@Composable
private fun PreviewPrintOrder() {

    val context = LocalContext.current
    val render = remember {
        PrintOrderRenderInfo.from(context, fakePrintOrder())
    }

    JobFlowTheme {

        PrintOrder(render)
    }

}*/

fun fakePrintOrder(): PrintOrder {

    return PrintOrder().apply {

        printOrderNumber = 123456
        clientId = 1
        billingName = "Suri Graphix, Sivakasi"
        jobName = "NaiduHall Boxes J00486/22-23"
        paperDetails = mutableListOf(
            PaperDetail().apply {
                height = 58.5f
                width = 91f
                gsm = 100
                name = "Real Art Paper"
                sheets = 500
            },

            PaperDetail().apply {
                height = 58.5f
                width = 91f
                gsm = 130
                name = "Hikote Art Paper"
                sheets = 500
            }
        )
        plateMakingDetail = PlateMakingDetail().apply {
            plateNumber = 12568
            trimmingHeight = 579
            trimmingWidth = 904
            jobHeight = 564
            jobWidth = 904
            gripper = 10
            tail = 5
            backsidePrinting = "Gripper to Gripper"
            machine = "D3000S5"
            screen = "175# Final"
        }
        printingDetail = PrintingDetail().apply {
            colours = "4+4"
            printingInstructions = "Be careful about the density values"
            runningMinutes = 45
        }

        listPosition = currentTimeInMillis()
        invoiceDetails = "GST/25"
        lamination = Lamination().apply {
            material = Lamination.MATERIAL_BOPP
            micron = 10
            remarks = "Ask details"
        }

        cutting = ""
        packing = ""
    }

}