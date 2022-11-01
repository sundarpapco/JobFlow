package com.sivakasi.papco.jobflow.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.screens.destination.FixedDestinationFragment
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment
import com.sivakasi.papco.jobflow.screens.profile.ProfileScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalMaterialApi
val LocalBottomSheetState =
    compositionLocalOf<ModalBottomSheetState> { error("Bottom sheet state must be provided") }
val LocalRole = compositionLocalOf<String> { error("Role must be provided") }
val LocalNavigation = compositionLocalOf<NavController> { error("Navigation must be initialized") }
val LocalSignOut = compositionLocalOf<() -> Unit> { error("Logout function not set") }

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    role: String,
    jobGroups: List<JobGroupState>,
    navController: NavController,
    onSignOut: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    CompositionLocalProvider(
        LocalNavigation provides navController,
        LocalBottomSheetState provides bottomSheetState,
        LocalRole provides role,
        LocalSignOut provides onSignOut
    ) {
        JobFlowTheme {

            val user = remember(role) { JobFlowAuth().currentUser }

            ModalBottomSheetLayout(
                sheetContent = {
                    ProfileScreen(
                        name = user?.displayName ?: "null",
                        email = user?.email ?: "null",
                        role = role
                    )
                },
                sheetState = bottomSheetState,
                scrimColor = MaterialTheme.colors.background.copy(alpha = 0.3f),
                sheetShape = RoundedCornerShape(20.dp, 20.dp)
            ) {
                HomeScreenContent(jobGroups = jobGroups)
            }

        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
private fun HomeScreenContent(
    jobGroups: List<JobGroupState>
) {

    val navController = LocalNavigation.current
    val role = LocalRole.current

    Scaffold(
        topBar = {
            HomeScreenTopBar()
        }
    ) {

        JobGroupList(jobGroups = jobGroups, onJobGroupClicked = {
            onJobGroupClicked(it, navController, role)
        })
    }
}


@ExperimentalMaterialApi
@Composable
fun JobGroupList(
    jobGroups: List<JobGroupState>,
    onJobGroupClicked: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {
                Spacer(Modifier.height(24.dp))
            }

            itemsIndexed(jobGroups) { index, state ->
                JobGroup(
                    state = state,
                    onClick = { onJobGroupClicked(index) }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


@ExperimentalMaterialApi
@Composable
fun JobGroup(
    state: JobGroupState,
    onClick: () -> Unit
) {

    Card(
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.secondaryVariant),
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = state.iconResourceId),
                contentDescription = "Home screen Icon",
                modifier = Modifier.size(65.dp),
                tint = MaterialTheme.colors.secondary
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = state.groupName,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.jobCount,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.jobTime,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun HomeScreenTopBar() {

    val context = LocalContext.current
    val role = LocalRole.current
    val navController = LocalNavigation.current
    val bottomSheetState = LocalBottomSheetState.current
    val signOut = LocalSignOut.current
    val menuItems = remember(role) { prepareOptionsMenu(role, context) }
    val scope = rememberCoroutineScope()

    JobFlowTopBar(
        title = stringResource(id = R.string.papco_jobs),
        actions = {
            OptionsMenu(menuItems = menuItems, onItemClick = {
                onOptionsItemClicked(
                    it,
                    context,
                    navController,
                    bottomSheetState,
                    scope,
                    signOut
                )
            })
        }
    )
}

private fun prepareOptionsMenu(role: String, context: Context): List<MenuAction> {

    val menuList = LinkedList<MenuAction>()

    with(menuList) {
        add(MenuAction(null, Icons.Outlined.Search, context.getString(R.string.search)))
        add(MenuAction(null, Icons.Outlined.Person, context.getString(R.string.Profile)))
        add(MenuAction(null, null, context.getString(R.string.clients)))
        add(MenuAction(null, null, context.getString(R.string.client_history)))
        add(MenuAction(null, null, context.getString(R.string.invoice_history)))
        if (role == "root")
            add(MenuAction(null, null, context.getString(R.string.change_user_role)))
        add(MenuAction(null, null, context.getString(R.string.sign_out)))
    }

    return menuList

}

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
private fun onOptionsItemClicked(
    clickedItemLabel: String,
    context: Context,
    navController: NavController,
    bottomSheetState: ModalBottomSheetState,
    scope: CoroutineScope,
    onSignOut: () -> Unit
) {
    when (clickedItemLabel) {
        context.getString(R.string.search) -> {
            //navController.navigate(R.id.action_fragmentHome_to_searchFragment)
            navController.navigate(R.id.action_fragmentHome_to_algoliaSearchFragment)
        }

        context.getString(R.string.Profile) -> {
            scope.launch {
                bottomSheetState.show()
            }
        }

        context.getString(R.string.clients) -> {
            navController.navigate(R.id.action_fragmentHome_to_clientsFragment)
        }

        context.getString(R.string.client_history) -> {
            navController.navigate(
                R.id.action_fragmentHome_to_clientsFragment,
                ClientsFragment.getArguments(true)
            )
        }

        context.getString(R.string.invoice_history) -> {
            navController.navigate(R.id.action_fragmentHome_to_invoiceHistoryFragment)
        }

        context.getString(R.string.change_user_role) -> {
            navController.navigate(R.id.action_fragmentHome_to_updateRoleFragment)
        }

        context.getString(R.string.sign_out) -> {
            onSignOut()
        }
    }
}

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
private fun onJobGroupClicked(
    index: Int,
    navController: NavController,
    role: String
) {

    when (index) {

        //When New Jobs clicked
        0 -> {
            navController.navigate(R.id.action_fragmentHome_to_fixedDestinationFragment)
        }

        //When In Progress Clicked
        1 -> {
            navController.navigate(
                R.id.action_fragmentHome_to_fixedDestinationFragment,
                FixedDestinationFragment.getArgumentBundle(
                    DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                    Destination.TYPE_FIXED
                )
            )
        }

        //When Machines Clicked
        2 -> {
            navigateToMachinesFragment(role, navController)
        }
    }
}

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
private fun navigateToMachinesFragment(
    role: String,
    navController: NavController
) {

    if (role == "printer") {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.fragmentHome, true)
            .build()
        navController.navigate(
            R.id.action_fragmentHome_to_manageMachinesFragment,
            ManageMachinesFragment.getArguments(false),
            navOptions
        )
    } else {
        navController.navigate(
            R.id.action_fragmentHome_to_manageMachinesFragment,
            ManageMachinesFragment.getArguments(false)
        )
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
private fun JobGroupPreview() {

    val state = JobGroupState()


    state.iconResourceId = R.drawable.ic_new_jobs
    state.groupName = "New Jobs"
    state.jobCount = "7 Jobs"
    state.jobTime = "9 Hours, 18 Minutes"

    JobFlowTheme {
        JobGroup(state = state) {

        }
    }
}

/*@ExperimentalMaterialApi
@Preview
@Composable
private fun HomeScreenPreview() {

    val state = JobGroupState()

    state.iconResourceId = R.drawable.ic_new_jobs
    state.groupName = "New Jobs"
    state.jobCount = "7 Jobs"
    state.jobTime = "9 Hours, 18 Minutes"

    JobFlowTheme {
        HomeScreen(role = "root",
            jobGroups = listOf(state, state, state),
            onJobGroupClicked = {}
        )
    }
}*/

