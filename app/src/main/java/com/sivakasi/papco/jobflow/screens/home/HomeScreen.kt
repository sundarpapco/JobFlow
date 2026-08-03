package com.sivakasi.papco.jobflow.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sivakasi.papco.jobflow.ui.JobFlowMaterial3Theme
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.ClientSelectionPurpose
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.nav3.LocalUserClaim
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.screens.profile.ProfileScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTopBar
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.LinkedList

@OptIn(
     ExperimentalCoroutinesApi::class,
    ExperimentalComposeUiApi::class, FlowPreview::class
)
fun EntryProviderScope<NavKey>.homeScreenEntry(
    navBackStack: NavBackStack<NavKey>,
    onSignOut: () -> Unit
) {
    entry<AppGraph.Home> {

        val viewModel: FragmentHomeVM = hiltViewModel()

        HomeScreen(
            jobGroups = viewModel.getStates(),
            navBackstack = navBackStack,
            onSignOut = onSignOut
        )
        
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi

@Composable
fun HomeScreen(
    jobGroups: List<JobGroupState>,
    navBackstack: NavBackStack<NavKey>,
    onSignOut: () -> Unit
) {

    val bottomSheetState =
        rememberModalBottomSheetState()

    var bottomSheetShowing by rememberSaveable { mutableStateOf(false) }

    val role = LocalUserClaim.current
    val user = remember(role) { JobFlowAuth().currentUser }

    HomeScreenContent(
        jobGroups = jobGroups,
        backStack = navBackstack,
        onSignOut = onSignOut,
        onProfileClicked = {
            bottomSheetShowing = true
        }
    )


    if (bottomSheetShowing) {
        ModalBottomSheet(
            onDismissRequest = { bottomSheetShowing = false },
            sheetState = bottomSheetState
        ) {
            ProfileScreen(
                name = user?.displayName ?: "null",
                email = user?.email ?: "null",
                role = role ?: "none"
            )
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi

@Composable
private fun HomeScreenContent(
    jobGroups: List<JobGroupState>,
    backStack: NavBackStack<NavKey>,
    onSignOut: () -> Unit,
    onProfileClicked: () -> Unit
) {

    Scaffold(
        topBar = {
            HomeScreenTopBar(backStack, onSignOut, onProfileClicked)
        }
    ) { paddingValues ->

        JobGroupList(
            modifier = Modifier.padding(paddingValues),
            jobGroups = jobGroups,
            onJobGroupClicked = { index ->
                when (index) {
                    0 -> {
                        backStack.add(
                            AppGraph.Destination(
                                DatabaseContract.DOCUMENT_DEST_NEW_JOBS,
                                Destination.TYPE_FIXED
                            )
                        )
                    }

                    1 -> {
                        backStack.add(
                            AppGraph.Destination(
                                DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                                Destination.TYPE_FIXED
                            )
                        )
                    }

                    2 -> {
                        backStack.add(AppGraph.Machines(false))
                    }
                }
            })
    }
}



@Composable
fun JobGroupList(
    modifier: Modifier = Modifier,
    jobGroups: List<JobGroupState>,
    onJobGroupClicked: (Int) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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



@Composable
fun JobGroup(
    state: JobGroupState,
    onClick: () -> Unit
) {

    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                tint = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = state.groupName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.jobCount,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.jobTime,
                    style = MaterialTheme.typography.titleSmall,
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

@Composable
private fun HomeScreenTopBar(
    backStack: NavBackStack<NavKey>,
    onSignOut: () -> Unit,
    onProfileClicked: () -> Unit
) {

    val context = LocalContext.current
    val role = LocalUserClaim.current ?: "none"
    val menuItems = remember(role) { prepareOptionsMenu(role, context) }

    JobFlowTopBar(
        title = stringResource(id = R.string.papco_jobs),
        actions = {
            OptionsMenu(menuItems = menuItems, onItemClick = {
                onOptionsItemClicked(
                    it,
                    context,
                    backStack,
                    onProfileClicked,
                    onSignOut
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

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalComposeUiApi

private fun onOptionsItemClicked(
    clickedItemLabel: String,
    context: Context,
    backStack: NavBackStack<NavKey>,
    onProfileClicked: () -> Unit,
    onSignOut: () -> Unit
) {
    when (clickedItemLabel) {
        context.getString(R.string.search) -> {
            backStack.add(AppGraph.AlgoliaSearch)
        }

        context.getString(R.string.Profile) -> {
            onProfileClicked()
        }

        context.getString(R.string.clients) -> {
            backStack.add(AppGraph.Client())
        }

        context.getString(R.string.client_history) -> {
            backStack.add(AppGraph.Client(ClientSelectionPurpose.History))
        }

        context.getString(R.string.invoice_history) -> {
            backStack.add(AppGraph.InvoiceHistory)
        }

        context.getString(R.string.change_user_role) -> {
            backStack.add(AppGraph.UpdateRole)
        }

        context.getString(R.string.sign_out) -> {
            onSignOut()
        }
    }
}


@Preview
@Composable
private fun JobGroupPreview() {

    val state = JobGroupState()


    state.iconResourceId = R.drawable.ic_new_jobs
    state.groupName = "New Jobs"
    state.jobCount = "7 Jobs"
    state.jobTime = "9 Hours, 18 Minutes"

    JobFlowMaterial3Theme {
        JobGroup(state = state) {

        }
    }
}

/*
@Preview
@Composable
private fun HomeScreenPreview() {

    val state = JobGroupState()

    state.iconResourceId = R.drawable.ic_new_jobs
    state.groupName = "New Jobs"
    state.jobCount = "7 Jobs"
    state.jobTime = "9 Hours, 18 Minutes"

    JobFlowMaterial3Theme {
        HomeScreen(role = "root",
            jobGroups = listOf(state, state, state),
            onJobGroupClicked = {}
        )
    }
}*/

