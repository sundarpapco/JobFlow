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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
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
import androidx.navigation.NavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.screens.clients.ui.LoadingScreen
import com.sivakasi.papco.jobflow.screens.destination.FixedDestinationFragment
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment.Companion.KEY_SELECTED_MACHINE_ID
import com.sivakasi.papco.jobflow.screens.profile.ProfileScreen
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
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import com.sivakasi.papco.jobflow.util.LoadingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch


val LocalNavigation = compositionLocalOf<NavController> { error("Navigation controller not set") }

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
val LocalViewModel = compositionLocalOf<ManageMachinesVM> { error("ViewModel not set") }
val LocalSignOut = compositionLocalOf<() -> Unit> { error("Sign out function not set") }
val LocalSheetState = compositionLocalOf<ModalBottomSheetState> { error("Bottom Sheet state not set") }

@ExperimentalMaterialApi
val LocalState = compositionLocalOf<MachinesScreenUIState> { error("Machine state not set") }


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
    navController: NavController,
    onSignOut: () -> Unit,
    viewModel: ManageMachinesVM
) {

    val density = LocalDensity.current
    val bottomSheetState = remember{
        ModalBottomSheetState(ModalBottomSheetValue.Hidden,density)
    }

    CompositionLocalProvider(
        LocalNavigation provides navController,
        LocalSignOut provides onSignOut,
        LocalViewModel provides viewModel,
        LocalState provides viewModel.uiState,
        LocalSheetState provides bottomSheetState
    ) {
        val uiState = LocalState.current
        val user = remember(uiState.role) { JobFlowAuth().currentUser }

        JobFlowTheme {
            MachinesScreenContent(uiState.machines)
            ShowDialogs(uiState = uiState, viewModel = viewModel)

        }
    }

}

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun ShowDialogs(
    uiState: MachinesScreenUIState,
    viewModel: ManageMachinesVM
) {
    uiState.addMachineDialogState?.let {
        TextInputDialog(
            dialogState = it,
            onPositiveClick = {
                viewModel.addMachine()
            },
            onNegativeClick = {
                uiState.hideAddMachineDialog()
            }
        )
    }

    uiState.editMachineDialogState?.let {
        TextInputDialog(
            dialogState = it,
            onPositiveClick = {
                viewModel.editMachine()
            },
            onNegativeClick = {
                uiState.hideEditMachineDialog()
            }
        )
    }

    uiState.deletingMachineId?.let {
        JobFlowAlertDialog(
            message = stringResource(R.string.machine_delete_confirmation),
            positiveButtonText = stringResource(R.string.menu_delete),
            negativeButtonText= stringResource(R.string.cancel),
            onPositiveClick = {
                viewModel.deleteMachine(it)
                uiState.hideDeleteConfirmationDialog()
            },
            onNegativeClick = {
                uiState.hideDeleteConfirmationDialog()
            },
            onDismissListener = {
                uiState.hideDeleteConfirmationDialog()
            }
        )
    }

    if(uiState.isWaitDialogShowing)
        WaitDialog()
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
private fun MachinesScreenContent(
    machinesLoadingStatus: LoadingStatus
) {

    val uiState = LocalState.current

    Scaffold(
        topBar = {
            MachinesTopAppBar()
        },
        floatingActionButton = {
            if (uiState.shouldShowFloatingActionButton())
                JobFlowFloatingActionButton {
                    uiState.showAddMachineDialog()
                }
        }
    ) {
        when (machinesLoadingStatus) {

            is LoadingStatus.Loading -> {
                LoadingScreen()
            }


            is LoadingStatus.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                MachinesList(machines = machinesLoadingStatus.data as List<Destination>)
            }

            is LoadingStatus.Error -> {

            }

        }

    }
}


@ExperimentalMaterialApi
@Composable
private fun MachinesTopAppBar() {

    val uiState = LocalState.current

    if (uiState.selectionMode) {
        SelectionModeTopBar()
        return
    }

    if (uiState.role == "printer") {
        PrinterTopBar()
    } else
        AdminTopBar()
}


@ExperimentalMaterialApi
@Composable
private fun PrinterTopBar() {

    val context = LocalContext.current
    val signOut = LocalSignOut.current
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
                    signOut = signOut,
                    scope=scope
                )
            })
        }
    )
}

@Composable
private fun AdminTopBar() {

    val controller = LocalNavigation.current

    JobFlowTopBar(
        title = stringResource(id = R.string.machines),
        navigationIcon = {
            IconButton(onClick = { popUpBackStack(controller) }) {
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
private fun SelectionModeTopBar() {

    val controller = LocalNavigation.current
    JobFlowTopBar(
        title = stringResource(id = R.string.select_machine),
        navigationIcon = {
            IconButton(onClick = { popUpBackStack(controller) }) {
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
) {

    val context = LocalContext.current
    val uiState = LocalState.current
    val navController = LocalNavigation.current

    val menu = remember(uiState.role) {
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
                onClick = { onMachineClicked(it, uiState.selectionMode,navController) },
                optionsMenu = if (uiState.shouldShowContextMenu()) {
                    {
                        ContextMenu(
                            identifier = destination,
                            menuItems = menu
                        ) { label, identifier ->
                            onContextItemClicked(uiState, label, identifier)
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
    onClick: (String) -> Unit,
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
        onClick = { onClick(destination.id) }
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
        uiState.showDeleteConfirmationDialog(destination.id)
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

private fun popUpBackStack(navController: NavController) {
    navController.popBackStack()
}


@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
private fun onMachineClicked(
    machineId: String,
    selectionMode: Boolean,
    navController: NavController
) {

    if(selectionMode){
        navController.previousBackStackEntry?.savedStateHandle?.set(
            KEY_SELECTED_MACHINE_ID,
            machineId
        )
        navController.popBackStack()
    }else{
        navController.navigate(
            R.id.action_manageMachinesFragment_to_fixedDestinationFragment,
            FixedDestinationFragment.getArgumentBundle(machineId, Destination.TYPE_DYNAMIC)
        )
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

    val context = LocalContext.current
    val uiState = MachinesScreenUIState(context)

    JobFlowTheme {

        CompositionLocalProvider(
            LocalState provides uiState,
            LocalNavigation provides NavController(LocalContext.current)
        ) {
            MachinesTopAppBar()
        }

    }

}