package com.sivakasi.papco.jobflow.screens.machines

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.nav3.LocalUserClaim
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.nav3.util.ResultEventBus
import com.sivakasi.papco.jobflow.nav3.util.Toaster
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.ui.ContextMenu
import com.sivakasi.papco.jobflow.ui.JobFlowAlertDialog
import com.sivakasi.papco.jobflow.ui.JobFlowFloatingActionButton
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.ui.TextInputDialog
import com.sivakasi.papco.jobflow.ui.WaitDialog
import com.sivakasi.papco.jobflow.util.Duration
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class,
    ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, FlowPreview::class
)
fun EntryProviderScope<NavKey>.machinesScreenEntry(
    backstack: NavBackStack<NavKey>,
    result: ResultEventBus,
    onSignOut: () -> Unit
){
    entry<AppGraph.Machines> {key->

        val viewModel: ManageMachinesVM = hiltViewModel()

        ManageMachinesScreen(
            selectionMode = key.selectionMode,
            screenState = viewModel.uiState,
            backstack=backstack,
            result=result,
            onSignOut=onSignOut,
            onAddMachine = {viewModel.addMachine()},
            onEditMachine = {viewModel.editMachine()},
            onDeleteMachine = {viewModel.deleteMachine(it)}
        )

    }
}


val LocalSheetState = compositionLocalOf<ModalBottomSheetState> { error("Bottom Sheet state not set") }


private val machineNameText: TextStyle = TextStyle(
fontWeight = FontWeight.Normal,
fontSize = 24.sp,
letterSpacing = 0.sp
)

@ExperimentalFoundationApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun ManageMachinesScreen(
    selectionMode: Boolean,
    screenState: MachinesScreenUIState,
    backstack: NavBackStack<NavKey>,
    result: ResultEventBus,
    onSignOut: () -> Unit,
    onAddMachine:()->Unit,
    onEditMachine:()->Unit,
    onDeleteMachine:(String)->Unit
) {

    val density = LocalDensity.current
    val bottomSheetState = remember{
        ModalBottomSheetState(ModalBottomSheetValue.Hidden,density)
    }

    CompositionLocalProvider(LocalSheetState provides bottomSheetState) {

        val context = LocalContext.current

        MachinesScreenContent(
            selectionMode = selectionMode,
            screenState = screenState,
            onMachineClicked = { machine ->
                if (selectionMode) {
                    result.send(AppGraph.Machines.SELECTION_KEY, machine.id)
                    backstack.removeLastOrNull()
                } else {
                    backstack.add(AppGraph.Destination(machine.id, Destination.TYPE_DYNAMIC))
                }
            },
            onBack = { backstack.removeLastOrNull()},
            onSignOut = onSignOut
        )

        Toaster(context,screenState)

        when(val dialog=screenState.dialog){
            is MachinesScreenUIState.Dialog.None->{}
            is MachinesScreenUIState.Dialog.AddMachineDialog->{
                TextInputDialog(
                    dialogState = dialog.state,
                    onPositiveClick = {onAddMachine()},
                    onNegativeClick = {screenState.clearDialog() }
                )
            }
            is MachinesScreenUIState.Dialog.EditMachineDialog->{
                TextInputDialog(
                    dialogState = dialog.state,
                    onPositiveClick = { onEditMachine() },
                    onNegativeClick = { screenState.clearDialog() }
                )
            }
            is MachinesScreenUIState.Dialog.DeleteConfirmationDialog->{
                JobFlowAlertDialog(
                    message = stringResource(R.string.machine_delete_confirmation),
                    positiveButtonText = stringResource(R.string.menu_delete),
                    negativeButtonText= stringResource(R.string.cancel),
                    onPositiveClick = { onDeleteMachine(dialog.deletingDestination.id) },
                    onNegativeClick = {screenState.clearDialog() },
                    onDismissListener = {screenState.clearDialog() }
                )
            }
            is MachinesScreenUIState.Dialog.WaitDialog -> { WaitDialog()}
        }
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
private fun MachinesScreenContent(
    selectionMode: Boolean,
    screenState: MachinesScreenUIState,
    onMachineClicked:(Destination)->Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {

    val role = LocalUserClaim.current

    Scaffold(
        topBar = {
            MachinesTopAppBar(selectionMode,onBack,onSignOut)
        },
        floatingActionButton = {
            if(!selectionMode){
                if(role=="admin" || role=="root")
                    JobFlowFloatingActionButton {
                        screenState.showAddMachineDialog()
                    }
            }
        }
    ) {

        when (val loadingState = screenState.machines) {

            is LoadingStatus.Loading -> {
                LoadingScreen()
            }


            is LoadingStatus.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                MachinesList(
                    machines = loadingState.data as List<Destination>,
                    screenState = screenState,
                    onClick = { onMachineClicked(it) },
                    shouldShowContextMenu = !selectionMode && (role == "admin" || role == "root")
                )
            }

            is LoadingStatus.Error -> {

            }

        }

    }
}


@ExperimentalMaterialApi
@Composable
private fun MachinesTopAppBar(
    selectionMode: Boolean,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {

    val role = LocalUserClaim.current

    if (selectionMode) {
        SelectionModeTopBar(onBack)
        return
    }

    if (role == "printer") {
        PrinterTopBar(onSignOut)
    } else
        AdminTopBar(onBack)
}


@ExperimentalMaterialApi
@Composable
private fun PrinterTopBar(
    onSignOut: () -> Unit
) {

    val context = LocalContext.current
    val sheetState = LocalSheetState.current

    val scope = rememberCoroutineScope()
    val optionsMenu = remember { prepareOptionsMenu(context) }

    JobFlowTopBar(
        title = stringResource(id = R.string.machines),
        actions =
        {
            OptionsMenu(menuItems = optionsMenu, onItemClick = {
                onOptionsItemClicked(
                    context=context,
                    label = it,
                    bottomSheetState = sheetState,
                    signOut = onSignOut,
                    scope=scope
                )
            })
        }
    )
}

@Composable
private fun AdminTopBar(
    onBack:()->Unit
) {

    JobFlowTopBar(
        title = stringResource(id = R.string.machines),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    )
}

@Composable
private fun SelectionModeTopBar(
    onBack: () -> Unit
) {


    JobFlowTopBar(
        title = stringResource(id = R.string.select_machine),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    )
}

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun MachinesList(
    machines: List<Destination>,
    screenState: MachinesScreenUIState,
    shouldShowContextMenu: Boolean,
    onClick:(Destination)->Unit
) {

    val context = LocalContext.current
    val role = LocalUserClaim.current

    val menu = remember(role) {
        prepareContextMenu(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = machines,
            key = { it.id })
        { destination ->

            MachineListItem(
                destination = destination,
                onClick = { onClick(it) },
                optionsMenu = if (shouldShowContextMenu) {
                    {
                        ContextMenu(
                            identifier = destination,
                            menuItems = menu
                        ) { label, identifier ->
                            onContextItemClicked(screenState, label, identifier)
                        }
                    }
                } else
                    null
            )

        }
    }
}


@ExperimentalMaterialApi
@Composable
fun MachineListItem(
    destination: Destination,
    onClick: (Destination) -> Unit,
    optionsMenu: (@Composable () -> Unit)? = null
) {

    val duration = remember(destination.runningTime) {
        Duration.fromMinutes(destination.runningTime)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant),
        onClick = { onClick(destination) }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                contentDescription = "Gear Icon",
                tint = MaterialTheme.colors.secondary
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = destination.name,
                    style = machineNameText
                )

                Text(
                    text = stringResource(
                        id = R.string.duration_in_xx_jobs,
                        duration,
                        destination.jobCount
                    ),
                    style = MaterialTheme.typography.subtitle1
                )
            }

            optionsMenu?.let {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Top)
                ) {
                    it()
                }

            }
        }
    }

}


private fun prepareContextMenu(context: Context): List<MenuAction> {
    return listOf(
        MenuAction(null, null, context.getString(R.string.menu_edit)),
        MenuAction(null, null, context.getString(R.string.menu_delete))
    )
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
private fun onContextItemClicked(
    uiState: MachinesScreenUIState,
    itemString: String,
    destination: Destination
) {

    if (itemString == uiState.getString(R.string.menu_edit)) {
        uiState.showEditMachineDialog(destination)
    }

    if (itemString == uiState.getString(R.string.menu_delete)) {
        uiState.showDeleteConfirmationDialog(destination)
    }

}

private fun prepareOptionsMenu(context: Context): List<MenuAction> {
    return listOf(
        MenuAction(null, Icons.Outlined.Person, context.getString(R.string.Profile)),
        MenuAction(null, null, context.getString(R.string.sign_out))
    )
}

@ExperimentalMaterialApi
private fun onOptionsItemClicked(
    context: Context,
    label: String,
    bottomSheetState: ModalBottomSheetState,
    signOut:()->Unit,
    scope:CoroutineScope
) {

    when(label){

        context.getString(R.string.sign_out)->{
            signOut()
        }

        context.getString(R.string.Profile)->{
            scope.launch{
                bottomSheetState.show()
            }
        }

    }

}

@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewMachineAListItem() {

    val context = LocalContext.current
    val menu = remember { prepareContextMenu(context) }

    val destination = Destination().apply {
        name = "D3000S5"
        runningTime = 500
        jobCount = 14
    }

    JobFlowTheme {
        MachineListItem(destination, {}) {
            OptionsMenu(menuItems = menu, onItemClick = {})
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun PreviewTopAppBar() {

    JobFlowTheme {
        MachinesTopAppBar(false,{},{})
    }

}