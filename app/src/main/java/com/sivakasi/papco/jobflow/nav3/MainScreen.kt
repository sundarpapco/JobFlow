package com.sivakasi.papco.jobflow.nav3


import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sivakasi.papco.jobflow.MainActivityVM
import com.sivakasi.papco.jobflow.admin.selectUserScreenEntry
import com.sivakasi.papco.jobflow.admin.updateRoleScreenEntry
import com.sivakasi.papco.jobflow.nav3.graph.AppGraph
import com.sivakasi.papco.jobflow.nav3.graph.PrintOrderGraph
import com.sivakasi.papco.jobflow.nav3.util.rememberResultEventBus
import com.sivakasi.papco.jobflow.preview.managePreviewsScreenEntry
import com.sivakasi.papco.jobflow.preview.view.viewPreviewScreenEntry
import com.sivakasi.papco.jobflow.screens.clients.history.clientHistoryEntry
import com.sivakasi.papco.jobflow.screens.clients.ui.clientsEntry
import com.sivakasi.papco.jobflow.screens.destination.destinationScreenEntry
import com.sivakasi.papco.jobflow.screens.home.homeScreenEntry
import com.sivakasi.papco.jobflow.screens.invoicehistory.invoiceHistoryEntry
import com.sivakasi.papco.jobflow.screens.login.forgotPasswordEntry
import com.sivakasi.papco.jobflow.screens.login.guestScreenEntry
import com.sivakasi.papco.jobflow.screens.login.loginScreenEntry
import com.sivakasi.papco.jobflow.screens.login.noInternetEntry
import com.sivakasi.papco.jobflow.screens.login.splashScreenEntry
import com.sivakasi.papco.jobflow.screens.machines.machinesScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.printOrderFlowEntry
import com.sivakasi.papco.jobflow.screens.notes.notesScreenEntry
import com.sivakasi.papco.jobflow.screens.processinghistory.previousHistoryScreenEntry
import com.sivakasi.papco.jobflow.screens.search.searchEntry
import com.sivakasi.papco.jobflow.screens.viewprintorder.viewPrintOrderEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun MainScreen() {

    val viewModel: MainActivityVM = hiltViewModel()
    val backStack = rememberNavBackStack(AppGraph.Splash)
    val resultBus = rememberResultEventBus()
    val claimDecorator = remember(viewModel.userClaim) {
        UserClaimNavEntryDecorator(viewModel.userClaim)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally { it / 5 } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 5 } + fadeOut()
        },
        popTransitionSpec = {
            slideInHorizontally { -it / 5 } + fadeIn() togetherWith
                    slideOutHorizontally { it / 5 } + fadeOut()
        },
        predictivePopTransitionSpec = {
            slideInHorizontally { -it / 5 } + fadeIn() togetherWith
                    slideOutHorizontally { it / 5 } + fadeOut()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            claimDecorator
        ),
        entryProvider = entryProvider {
            splashScreenEntry()
            guestScreenEntry(viewModel.auth)
            loginScreenEntry(backStack)
            noInternetEntry(viewModel.isInitializing,viewModel::initializeCurrentUser)
            forgotPasswordEntry()
            homeScreenEntry(backStack){ viewModel.auth.logout() }
            machinesScreenEntry(backStack,resultBus){viewModel.auth.logout()}
            destinationScreenEntry(backStack,resultBus)
            viewPrintOrderEntry(backStack)
            notesScreenEntry(backStack)
            previousHistoryScreenEntry(backStack)
            managePreviewsScreenEntry(backStack)
            viewPreviewScreenEntry(backStack)
            searchEntry(backStack)
            clientsEntry(backStack,resultBus)
            clientHistoryEntry(backStack)
            invoiceHistoryEntry(backStack)
            updateRoleScreenEntry(backStack,resultBus)
            selectUserScreenEntry(backStack,resultBus)
            printOrderFlowEntry(onExitFlow = {backStack.popUntil(true) { it is PrintOrderGraph }})
        }
    )

    LaunchedEffect(Unit) {
        viewModel.landingHomeScreen.collect {
            if (it !is AppGraph.Splash) {
                backStack.add(it)
                if (backStack.size > 1) {
                    backStack.subList(0, backStack.size - 1).clear()
                }
            }
        }
    }

}


fun NavBackStack<NavKey>.popUntil(inclusive: Boolean = false, predicate: (NavKey) -> Boolean) {
    val index = this.indexOfFirst(predicate)

    if (index == -1)
        return

    if (inclusive) {
        this.subList(index, this.size).clear()
    } else {
        if (index == this.size - 1)
            return
        else
            this.subList(index + 1, this.size).clear()
    }
}

fun NavBackStack<NavKey>.replaceLastOrAdd(destination: NavKey){
    if(isEmpty())
        add(destination)
    else{
        this[this.size-1]=destination
    }
}