package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentJobDetailsBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.util.FormValidator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentJobDetails : Fragment() {

    private var _viewBinding: FragmentJobDetailsBinding? = null
    private val viewBinding: FragmentJobDetailsBinding
        get() = _viewBinding!!

    private var printOrder = PrintOrder()
    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentJobDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackAsClose()
        initViews()
        observeViewModel()

        if (viewModel.isEditMode)
            updateTitle(getString(R.string.edit_job))
        else
            updateTitle(getString(R.string.create_job))
        updateSubTitle("")
        registerBackArrowMenu()
    }

    override fun onPause() {
        super.onPause()
        saveJobDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        //If this screen is editing a print order and the editing po is already completed,
        //then show the Invoice details which also can be edited here
        if (viewModel.isEditMode &&
            viewModel.editingPrintOrderParentDestinationId == DatabaseContract.DOCUMENT_DEST_COMPLETED
        ) {
            viewBinding.txtLayoutInvoiceDetail.visibility=View.VISIBLE
        }else{
            viewBinding.txtLayoutInvoiceDetail.visibility=View.GONE
        }

        viewBinding.txtClientName.setOnClickListener {
            navigateToClientSelectionScreen()
        }

        viewBinding.btnNext.setOnClickListener {
            if (validateForm()) {
                saveJobDetails()
                hideKeyboard(requireContext(), viewBinding.btnNext)
                navigateToNextScreen()
            }
        }

        viewBinding.txtClientName.clearErrorOnTextChange()
        viewBinding.txtJobName.clearErrorOnTextChange()
        viewBinding.txtInvoiceDetail.clearErrorOnTextChange()
    }

    private fun observeViewModel() {

        viewModel.recoveringFromProcessDeath.observe(viewLifecycleOwner){
            if(it)
                exitOutOfCreationFlow()
        }

        viewModel.loadedJob.observe(viewLifecycleOwner) {
            printOrder = it
            renderJobDetails()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Client>(
            ClientsFragment.KEY_CLIENT
        )?.observe(viewLifecycleOwner) {

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Client>(
                ClientsFragment.KEY_CLIENT
            )

            printOrder.billingName = it.name
            printOrder.clientId = it.id
            renderJobDetails()
        }
    }

    private fun renderJobDetails() {
        viewBinding.switchUrgent.isChecked = printOrder.emergency
        viewBinding.txtJobName.setText(printOrder.jobName)
        viewBinding.txtClientName.setText(printOrder.billingName)
        viewBinding.txtPendingRemarks.setText(printOrder.pendingRemarks)

        //The invoice detail field will be visible only when a root is editing an already completed PO
        //So, check whether its visible and only then we can update the field
        if(viewBinding.txtLayoutInvoiceDetail.visibility==View.VISIBLE)
            viewBinding.txtInvoiceDetail.setText(printOrder.invoiceDetails)
    }

    private fun validateForm(): Boolean {

        /*
        This is a special case validation because clientID was introduced as late feature.
        The following validation will force the user to select a valid client with an Id while editing the PO.
        If the user is editing an old Print Order, then that print order will have a valid client name.
        But the id will be -1. The following condition will check that condition and will force user to select
        a valid client so that, that PO can be searched in client history in future.
         */
        val clientName = viewBinding.txtClientName.text.toString()
        if (clientName.isNotBlank() && printOrder.clientId == -1) {
            //Will prompt the user to select the client again so that we can get the Id of the client
            viewBinding.textLayoutClientName.error = getString(R.string.invalid_client_name)
            return false
        }

        val errorMsg = getString(R.string.required_field)
        val validator = FormValidator()
            .validate(viewBinding.txtClientName.validateForNonBlank(errorMsg))
            .validate(viewBinding.txtJobName.validateForNonBlank(errorMsg))

        //The invoice detail field will be visible only when a root is editing an already completed PO
        //So, check whether its visible and if yes, then validate it
        if(viewBinding.txtLayoutInvoiceDetail.visibility==View.VISIBLE)
            validator.validate(viewBinding.txtInvoiceDetail.validateForNonBlank(errorMsg))

        return validator.isValid()

    }


    private fun saveJobDetails() {

        with(viewBinding) {
            printOrder.emergency = switchUrgent.isChecked
            printOrder.jobName = txtJobName.text.toString().trim()
            printOrder.billingName = txtClientName.text.toString().trim()
            printOrder.pendingRemarks = txtPendingRemarks.text.toString()

            if(txtLayoutInvoiceDetail.visibility==View.VISIBLE)
                printOrder.invoiceDetails = txtInvoiceDetail.text.toString().trim()
        }
    }

    private fun navigateToClientSelectionScreen() {
        findNavController().navigate(
            R.id.action_fragmentJobDetails_to_clientSelectionFragment,
            ClientsFragment.getArguments(true)
        )
    }

    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }

    private fun navigateToNextScreen() =
        findNavController().navigate(R.id.action_fragmentJobDetails_to_fragmentPaperDetails)


}