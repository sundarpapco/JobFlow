package com.sivakasi.papco.jobflow.screens.destination

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.asReadableTimeStamp
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import com.sivakasi.papco.jobflow.ui.WaitDialog
import com.sivakasi.papco.jobflow.util.Duration
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun DestinationScreen(
    screenState: DestinationScreenState,
    fixedDestination: Boolean,
    onClicked: (PrintOrderUIModel) -> Unit,
    onBack: () -> Unit,
    onDragCompleted: (List<PrintOrderUIModel>) -> Unit,
    onAddJob: (() -> Unit)?,
    onAllotJobs: () -> Unit,
    onInvoiceJobs: (String, Boolean) -> Unit,
    onMarkAsPending: (String) -> Unit,
    onDeleteJobs: () -> Unit,
    onRevertJobs: () -> Unit,
    onMarkAsDone: () -> Unit,
    onClearPending: (PrintOrderUIModel?) -> Unit
) {

    BackHandler(
        enabled = screenState.selection.selectionCount > 0
    ) {
        screenState.selection.clear()
    }

    val context = LocalContext.current
    val snackBarState = remember { SnackbarHostState() }
    val actionModeActionBar by remember {
        derivedStateOf{screenState.selection.selectionCount>0}
    }
    val jobsLoaded by remember {
        derivedStateOf{screenState.jobs!=null}
    }

    Scaffold(
        topBar = {
            Crossfade(
                targetState = actionModeActionBar
            ) { actionMode ->
                if (!actionMode)
                    StandardTopBar(screenState, onBack)
                else
                    if (fixedDestination)
                        FixedDestinationActionBar(
                            screenState = screenState,
                            onClose = { screenState.selection.clear() },
                            onOptionClicked = {
                                onOptionsItemSelected(
                                    context,
                                    it,
                                    screenState,
                                    onAllotJobs,
                                    onClearPending
                                )
                            }
                        )
                    else
                        DynamicDestinationActionBar(
                            screenState = screenState,
                            onClose = { screenState.selection.clear() },
                            onOptionClicked = {
                                onOptionsItemSelected(
                                    context,
                                    it,
                                    screenState,
                                    onAllotJobs,
                                    onClearPending
                                )
                            }
                        )
            }
        },
        floatingActionButton = {
            onAddJob?.let {
                FloatingActionButton(
                    backgroundColor = MaterialTheme.colors.primary,
                    onClick = it,
                ) {
                    Icon(
                        contentDescription = "Add new Job",
                        imageVector = Icons.Outlined.Add
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarState)
        }
    ) { paddingValues ->

        Crossfade(
            targetState = jobsLoaded
        ) { loaded->
            if(loaded)
                DestinationJobList(
                    screenState = screenState,
                    snackBarState = snackBarState,
                    jobs = screenState.jobs!!,
                    fixedDestination = fixedDestination,
                    onDragCompleted = onDragCompleted,
                    onClicked=onClicked,
                    onClearPending=onClearPending,
                    modifier = Modifier.padding(paddingValues)
                )
            else
                LoadingScreen(modifier = Modifier.padding(paddingValues))
        }
    }

    DestinationDialog(
        screenState = screenState,
        onInvoiceConfirmation = onInvoiceJobs,
        onDeleteConfirmation = onDeleteJobs,
        onRevertConfirmation = onRevertJobs,
        onMarkAsDoneConfirmation = onMarkAsDone,
        onMarkAsPendingConfirmation = onMarkAsPending
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DestinationJobList(
    screenState: DestinationScreenState,
    snackBarState:SnackbarHostState,
    jobs:List<PrintOrderUIModel>,
    fixedDestination: Boolean,
    onDragCompleted:(List<PrintOrderUIModel>)->Unit,
    onClicked:(PrintOrderUIModel)->Unit,
    onClearPending: (PrintOrderUIModel?) -> Unit,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            screenState.dragJob(from.index - 1, to.index - 1)
        }

    Column(
        modifier = modifier
    ) {
        if (!fixedDestination)
            LastJobCompletedAt(screenState.destination.lastJobCompletion)

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
            }
            items(
                jobs,
                key = { model ->
                    model.printOrderNumber
                }
            ) { model ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = model.printOrderNumber
                ) { dragging ->
                    JobListItem(
                        printOrder = model,
                        selected = screenState.selection.selectionCount > 0 &&
                                screenState.selection.contains(model.printOrderNumber),
                        dragging = dragging,
                        dragEnabled = screenState.selection.selectionCount == 0,
                        onDragCompleted = {
                            val jobsToUpdate = screenState.onDragCompleted()
                            jobsToUpdate?.let {
                                onDragCompleted(it)
                            }
                        },
                        onClick = {
                            if (screenState.selection.selectionCount > 0)
                                screenState.selection.toggle(model)
                            else
                                onClicked(model)
                        },
                        onLongClick = {
                            screenState.selection.toggle(model)
                        },
                        onPendingIconClicked = {
                            scope.launch {
                                val result = snackBarState.showSnackbar(
                                    message = model.pendingReason,
                                    actionLabel = context.getString(R.string.clear)
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onClearPending(model)
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun DestinationDialog(
    screenState: DestinationScreenState,
    onInvoiceConfirmation: (String, Boolean) -> Unit,
    onDeleteConfirmation: () -> Unit,
    onRevertConfirmation: () -> Unit,
    onMarkAsDoneConfirmation: () -> Unit,
    onMarkAsPendingConfirmation: (String) -> Unit
) {
    when (screenState.dialog) {
        is DestinationScreenDialog.None -> {

        }

        is DestinationScreenDialog.WaitDialog -> {
            WaitDialog()
        }

        is DestinationScreenDialog.InvoiceDetail -> {
            InvoiceDetailDialog(
                onPositiveClick = onInvoiceConfirmation,
                onDismiss = { screenState.hideDialog() }
            )
        }

        is DestinationScreenDialog.MarkAsPending -> {
            TextInputDialog(
                dialogState = (screenState.dialog as DestinationScreenDialog.MarkAsPending).dialogState,
                onPositiveClick = onMarkAsPendingConfirmation,
                onNegativeClick = { screenState.hideDialog() }
            )
        }

        is DestinationScreenDialog.DeleteConfirmation -> {
            JobFlowAlertDialog(
                message = stringResource(R.string.cancel_jobs_confirmation),
                positiveButtonText = stringResource(R.string.menu_delete),
                onPositiveClick = onDeleteConfirmation,
                onDismissListener = { screenState.hideDialog() }
            )
        }

        is DestinationScreenDialog.RevertConfirmation -> {
            JobFlowAlertDialog(
                message = stringResource(R.string.revert_confirmation),
                positiveButtonText = stringResource(R.string.remove),
                onPositiveClick = onRevertConfirmation,
                onDismissListener = { screenState.hideDialog() }
            )
        }

        is DestinationScreenDialog.MarkAsDoneConfirmation -> {
            JobFlowAlertDialog(
                message = stringResource(R.string.complete_confirmation),
                positiveButtonText = stringResource(R.string.mark_as_complete),
                onPositiveClick = onMarkAsDoneConfirmation,
                onDismissListener = { screenState.hideDialog() }
            )
        }

    }
}

@Composable
private fun StandardTopBar(
    screenState: DestinationScreenState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val subtitle = remember(screenState.destination) {
        val duration = Duration.fromMinutes(screenState.destination.runningTime).toString()
        context.getString(R.string.duration_in_xx_jobs, duration, screenState.destination.jobCount)
    }

    JobFlowTopBar(
        title = screenState.destination.name,
        subtitle = subtitle,
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}


@Composable
private fun FixedDestinationActionBar(
    screenState: DestinationScreenState,
    onClose: () -> Unit,
    onOptionClicked: (String) -> Unit
) {

    val context = LocalContext.current
    val menu = remember(screenState.selection.selectionCount) {
        val menu: MutableList<MenuAction> = mutableListOf()
        if (screenState.selection.showInvoiceOption)
            menu.add(
                MenuAction(
                    imageVector = Icons.Outlined.CurrencyRupee,
                    label = context.getString(R.string.invoice)
                )
            )
        menu.add(
            MenuAction(
                imageVector = Icons.Outlined.Upload,
                label = context.getString(R.string.menu_allot)
            )
        )

        if (screenState.selection.pendingJobSelectionCount == 0)
            menu.add(
                MenuAction(
                    imageVector = Icons.Filled.Error,
                    label = context.getString(R.string.mark_as_pending)
                )
            )
        else
            menu.add(
                MenuAction(
                    imageVector = Icons.Filled.CheckCircle,
                    label = context.getString(R.string.clear_pending)
                )
            )
        menu.add(MenuAction(label = context.getString(R.string.menu_delete)))

        menu
    }

    JobFlowTopBar(
        title = screenState.selection.title(),
        subtitle = screenState.selection.subTitle(),
        navigationIcon = {
            IconButton(
                onClick = onClose
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            OptionsMenu(
                menuItems = menu,
                onItemClick = onOptionClicked
            )
        }
    )
}

@Composable
private fun DynamicDestinationActionBar(
    screenState: DestinationScreenState,
    onClose: () -> Unit,
    onOptionClicked: (String) -> Unit
) {
    JobFlowTopBar(
        title = screenState.selection.title(),
        subtitle = screenState.selection.subTitle(),
        navigationIcon = {
            IconButton(
                onClick = onClose
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            OptionsMenu(
                menuItems = listOf(
                    MenuAction(
                        imageVector = Icons.AutoMirrored.Outlined.Reply,
                        label = stringResource(R.string.remove)
                    ),
                    MenuAction(
                        imageVector = Icons.Outlined.Done,
                        label = stringResource(R.string.mark_as_complete)
                    )
                ),
                onItemClick = onOptionClicked
            )
        }
    )
}

@Composable
private fun LastJobCompletedAt(
    completionTime: Long
) {
    val context = LocalContext.current
    val timeString = remember(completionTime) {
        context.getString(
            R.string.last_completed_on,
            calendarWithTime(completionTime).asReadableTimeStamp()
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeString,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
        )
    }
}

private fun onOptionsItemSelected(
    context: Context,
    item: String,
    screenState: DestinationScreenState,
    onAllotJobs: () -> Unit,
    onClearPending: (PrintOrderUIModel?) -> Unit
) {
    when (item) {
        context.getString(R.string.menu_allot) -> {
            onAllotJobs()
        }

        context.getString(R.string.invoice) -> {
            screenState.showInvoiceDialog()
        }

        context.getString(R.string.mark_as_pending) -> {
            screenState.showMarkAsPendingDialog()
        }

        context.getString(R.string.menu_delete) -> {
            screenState.showDeleteConfirmationDialog()
        }

        context.getString(R.string.clear_pending) -> {
            onClearPending(null)
        }

        context.getString(R.string.remove) -> {
            screenState.showRevertConfirmationDialog()
        }

        context.getString(R.string.mark_as_complete) -> {
            screenState.showMarkAsDoneConfirmationDialog()
        }
    }
}


@ExperimentalMaterialApi
@Preview
@Composable
private fun DestinationScreenPreview() {

    val context = LocalContext.current

    val screenState = remember {
        DestinationScreenState(context).apply {
            val jobs = (1..20).map {
                mockModel(it)
            }
            loadJobs(jobs)
        }
    }

    JobFlowTheme {
        DestinationScreen(
            screenState,
            fixedDestination = false,
            onClicked = {},
            onBack = {},
            onDragCompleted = {},
            onAddJob = {},
            onAllotJobs = {},
            onInvoiceJobs = { _, _ -> },
            onMarkAsPending = {},
            onMarkAsDone = {},
            onDeleteJobs = {},
            onRevertJobs = {},
            onClearPending = {}
        )
    }
}


private fun mockModel(index: Int): PrintOrderUIModel {

    return PrintOrderUIModel().apply {
        printOrderNumber = index
        billingName = "Suri Graphix, Sivakasi"
        jobName = "NaiduHall Brochure $index"
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