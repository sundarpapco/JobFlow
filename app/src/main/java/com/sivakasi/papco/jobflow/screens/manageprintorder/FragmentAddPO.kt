package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.manageprintorder.addJob.AddPrintOrderScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentAddPO : Fragment() {

    companion object {
        private const val KEY_EDITING_PO_ID = "key:editing:po:id"
        private const val KEY_PARENT_DESTINATION_ID = "key:parent:destination"
        private const val KEY_ARG_AUTO_REPEAT = "key:auto:load"

        fun getArgumentBundle(
            editingPONumber: Int,
            parentDestinationId: String,
            autoLoad: Boolean = false
        ): Bundle =
            Bundle().apply {
                putInt(KEY_EDITING_PO_ID, editingPONumber)
                putString(KEY_PARENT_DESTINATION_ID, parentDestinationId)
                putBoolean(KEY_ARG_AUTO_REPEAT, autoLoad)
            }
    }

    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        Auto repeat mode means that the user clicked "Repeat this job" in the
        menu of the ViewPrintOrder Screen. In this case, the printOrderNumber provided
        as argument will be used directly to create a reprint Job
        */
        if(isAutoRepeatMode()){
            check(getEditingPOId() > 0){"Invalid PO number provided in auto repeat mode"}
            //Search and load from repo using the provided PO number and not plate number
            viewModel.loadPrintOrderToEdit(getEditingPOId())
            return
        }

        if (isEditMode()) {
            viewModel.isEditMode = true
            viewModel.editingPrintOrderParentDestinationId = getParentDestinationId()
            viewModel.loadPrintOrderToEdit(getEditingPOId())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    AddPrintOrderScreen(
                        screenState = viewModel.addJobScreenState,
                        isEditMode = isEditMode(),
                        onCreateNewJob = viewModel::createNewJob,
                        onCreateRepeatJob = viewModel::createRepeatJob,
                        onLoadRepeatJob = viewModel::loadJobFromRepository,
                        onClose = {findNavController().popBackStack()}
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loadedJob.observe(viewLifecycleOwner) {
            //A valid print order has been successfully loaded. So, navigate to next screen
            navigateToNextScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.action_fragmentAddPO_to_fragmentJobDetails)
    }

    private fun isEditMode(): Boolean = getEditingPOId() != -4

    private fun isAutoRepeatMode(): Boolean =
        arguments?.getBoolean(KEY_ARG_AUTO_REPEAT) ?: false

    private fun getParentDestinationId(): String =
        arguments?.getString(KEY_PARENT_DESTINATION_ID) ?: DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    private fun getEditingPOId(): Int =
        arguments?.getInt(KEY_EDITING_PO_ID) ?: -4
}