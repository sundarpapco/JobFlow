package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentJobDetailsBinding
import com.sivakasi.papco.jobflow.extensions.enableBackAsClose
import com.sivakasi.papco.jobflow.extensions.hideKeyboard
import com.sivakasi.papco.jobflow.extensions.validateForNonBlank
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
    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack(R.id.fragmentJobDetails, true)
            return true
        }

        return super.onOptionsItemSelected(item)
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
    }

    private fun observeViewModel() {
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

            printOrder.billingName=it.name
            printOrder.clientId=it.id
            renderJobDetails()
        }
    }

    private fun renderJobDetails() {
        viewBinding.switchUrgent.isChecked = printOrder.emergency
        viewBinding.txtJobName.setText(printOrder.jobName)
        viewBinding.txtClientName.setText(printOrder.billingName)
        viewBinding.txtPendingRemarks.setText(printOrder.pendingRemarks)
    }

    private fun validateForm(): Boolean {

        /*
        This is a special case validation because clientID was introduced as late feature.
        The following validation will force the user to select a valid client with an Id while editing the PO.
        If the user is editing an old Print Order, then that print order will have a valid client name.
        But the id will be -1. The following condition will check that condition and will force user to select
        a valid client so that, that PO can be searched in client history in future.
         */
        val clientName=viewBinding.txtClientName.text.toString()
        if(clientName.isNotBlank() && printOrder.clientId ==-1){
            //Will prompt the user to select the client again so that we can get the Id of the client
            viewBinding.textLayoutClientName.error=getString(R.string.invalid_client_name)
            return false
        }

        val errorMsg = getString(R.string.required_field)
        val validator = FormValidator()
            .validate(viewBinding.txtClientName.validateForNonBlank(errorMsg))
            .validate(viewBinding.txtJobName.validateForNonBlank(errorMsg))

        return validator.isValid()

    }


    private fun saveJobDetails() {

        with(viewBinding) {
            printOrder.emergency = switchUrgent.isChecked
            printOrder.jobName = txtJobName.text.toString().trim()
            printOrder.billingName = txtClientName.text.toString().trim()
            printOrder.pendingRemarks = txtPendingRemarks.text.toString()
        }
    }

    private fun navigateToClientSelectionScreen(){
        findNavController().navigate(R.id.action_fragmentJobDetails_to_clientSelectionFragment,
        ClientsFragment.getArguments(true))
    }

    private fun navigateToNextScreen() =
        findNavController().navigate(R.id.action_fragmentJobDetails_to_fragmentPaperDetails)


}