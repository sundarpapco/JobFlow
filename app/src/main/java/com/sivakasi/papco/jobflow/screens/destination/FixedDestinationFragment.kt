package com.sivakasi.papco.jobflow.screens.destination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment
import com.sivakasi.papco.jobflow.screens.viewprintorder.ComposeViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FixedDestinationFragment : Fragment() {

    companion object {
        private const val KEY_DESTINATION_ID = "key:destination:id"
        private const val KEY_DESTINATION_TYPE = "key:destination:type"

        fun getArgumentBundle(destinationId: String, destinationType: Int): Bundle =
            Bundle().apply {
                putInt(KEY_DESTINATION_TYPE, destinationType)
                putString(KEY_DESTINATION_ID, destinationId)
            }
    }

    //Variable used to determine whether we should notifyDataSetChanged on the adapter when the action
    //mode finishes. When the action mode is finishing, this variable determines whether we should
    //refresh the adapter or not
    //True when the ActionMode is finishing cause of back arrow key and false when the action mode
    //is finishing as the result of allocation completion

    private val viewModel: FixedDestinationVM by lazy {
        ViewModelProvider(this)[FixedDestinationVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadJobsFromDestination(getDestinationId())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme {
                    DestinationScreen(
                        screenState = viewModel.screenState,
                        fixedDestination = getDestinationType() == Destination.TYPE_FIXED,
                        onBack = { findNavController().popBackStack() },
                        onDragCompleted = { viewModel.updateJobs(getDestinationId(), it) },
                        onAddJob = if (getDestinationId() == DatabaseContract.DOCUMENT_DEST_NEW_JOBS) {
                            { navigateToCreatePOScreen() }
                        } else
                            null,
                        onClicked = { navigateToViewPrintOrderScreen(it.printOrderNumber) },
                        onAllotJobs = { navigateToMachineSelectionScreen() },
                        onInvoiceJobs = { invoiceNumber, partialDispatch ->
                            if (!partialDispatch)
                                viewModel.invoiceSelectedJob(getDestinationId(), invoiceNumber)
                            else
                                viewModel.partDispatchSelectedJob(getDestinationId(), invoiceNumber)
                        },
                        onMarkAsPending = { viewModel.markAsPending(getDestinationId(), it) },
                        onDeleteJobs = { viewModel.cancelSelectedJobs(getDestinationId()) },
                        onRevertJobs = { viewModel.backtrackSelectedJobs(getDestinationId()) },
                        onMarkAsDone = { viewModel.markSelectedJobsAsComplete(getDestinationId()) },
                        onClearPending = {item-> viewModel.clearPendingStatus(getDestinationId(),item)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Unlock the selection first in case it may be locked
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @FlowPreview
    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi

    private fun observeViewModel() {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            ManageMachinesFragment.KEY_SELECTED_MACHINE_ID
        )?.observe(viewLifecycleOwner) {

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>(
                ManageMachinesFragment.KEY_SELECTED_MACHINE_ID
            )

            viewModel.allotSelectedJobs(getDestinationId(), it)
        }
    }

    private fun navigateToCreatePOScreen() {
        findNavController().navigate(R.id.action_fixedDestinationFragment_to_print_order_flow)
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun navigateToMachineSelectionScreen() {

        findNavController().navigate(
            R.id.action_fixedDestinationFragment_to_manageMachinesFragment,
            ManageMachinesFragment.getArguments(true)
        )
    }


    private fun navigateToViewPrintOrderScreen(printOrderNumber: Int) {
        findNavController().navigate(
            R.id.action_fixedDestinationFragment_to_composeViewPrintOrderFragment,
            ComposeViewPrintOrderFragment.getArguments(printOrderNumber)
        )
    }

    private fun getDestinationId(): String =
        arguments?.getString(KEY_DESTINATION_ID) ?: DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    private fun getDestinationType(): Int =
        arguments?.getInt(KEY_DESTINATION_TYPE) ?: Destination.TYPE_FIXED
}