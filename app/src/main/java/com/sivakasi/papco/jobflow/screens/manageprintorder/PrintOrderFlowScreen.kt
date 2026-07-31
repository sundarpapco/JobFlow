package com.sivakasi.papco.jobflow.screens.manageprintorder

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sivakasi.papco.jobflow.nav3.graph.PrintOrderGraph
import com.sivakasi.papco.jobflow.nav3.util.rememberResultEventBus
import com.sivakasi.papco.jobflow.screens.clients.ui.clientsEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.addJob.addPOScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails.jobDetailsScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.paperDetails.paperDetailsScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.plateMakingDetails.plateMakingScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.postpress.postPressScreenEntry
import com.sivakasi.papco.jobflow.screens.manageprintorder.printingDetails.printingDetailsScreenEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun EntryProviderScope<NavKey>.printOrderFlowEntry(
    onExitFlow: () -> Unit
) {
    entry<PrintOrderGraph>(
        metadata = metadata {
            put(NavDisplay.TransitionKey) {
                scaleIn(initialScale = 0.8f) + fadeIn() togetherWith scaleOut(targetScale = 1.1f) + fadeOut()
            }

            put(NavDisplay.PopTransitionKey) {
                scaleIn(initialScale = 1.1f) + fadeIn() togetherWith scaleOut(targetScale = 0.8f) + fadeOut()
            }
        }
    ) { key ->
        PrintOrderFlowScreen(key, onExitFlow)
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun PrintOrderFlowScreen(
    args: PrintOrderGraph,
    onExitFlow: () -> Unit
) {

    val viewModel: ManagePrintOrderVM = hiltViewModel()
    val resultBus = rememberResultEventBus()
    val backStack = rememberNavBackStack(
        PrintOrderGraph.AddPO(
            args.editingPONumber,
            args.parentDestinationId,
            args.autoRepeat
        )
    )

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
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            addPOScreenEntry(viewModel, backStack,onExitFlow)
            jobDetailsScreenEntry(viewModel, backStack, resultBus = resultBus, onExitFlow)
            clientsEntry(backStack, resultBus)
            paperDetailsScreenEntry(viewModel, backStack, onExitFlow)
            plateMakingScreenEntry(viewModel, backStack, onExitFlow)
            printingDetailsScreenEntry(viewModel, backStack, onExitFlow)
            postPressScreenEntry(viewModel, onExitFlow)
        }
    )

}