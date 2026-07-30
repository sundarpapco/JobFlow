package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.manageprintorder.addJob.AddPrintOrderScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        observeViewModel()
        /*
        Auto repeat mode means that the user clicked "Repeat this job" in the
        menu of the ViewPrintOrder Screen. In this case, the printOrderNumber provided
        as argument will be used directly to create a reprint Job
        */
        if(isAutoRepeatMode()){
            check(getEditingPOId() > 0){"Invalid PO number provided in auto repeat mode"}
            //Search and load from repo using the provided PO number and not plate number
            viewModel.loadJobByPONumber(getEditingPOId())
            return
        }

        if (isEditMode()) {
            viewModel.isEditMode = true
            viewModel.editingPrintOrderParentDestinationId = getParentDestinationId()
            viewModel.loadJobByPONumber(getEditingPOId())
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
                        onLoadRepeatJob = viewModel::loadJobByPlateNumber,
                        onClose = {findNavController().popBackStack()}
                    )
                }
            }
        }
    }

    private fun observeViewModel() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.loadedJob.collectLatest {
                    it?.let{
                        //A valid print order has been successfully loaded. So, navigate to next screen
                        navigateToNextScreen()
                    }
                }
            }
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