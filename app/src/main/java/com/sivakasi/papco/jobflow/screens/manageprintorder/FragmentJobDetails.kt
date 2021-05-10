package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentJobDetailsBinding
import com.sivakasi.papco.jobflow.extensions.enableBackAsClose
import com.sivakasi.papco.jobflow.extensions.hideKeyboard
import com.sivakasi.papco.jobflow.extensions.validateForNonBlank
import com.sivakasi.papco.jobflow.util.FormValidator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    }

    private fun renderJobDetails() {
        viewBinding.switchUrgent.isChecked = printOrder.emergency
        viewBinding.txtJobName.setText(printOrder.jobName)
        viewBinding.txtClientName.setText(printOrder.billingName)
        viewBinding.txtPendingRemarks.setText(printOrder.pendingRemarks)
    }

    private fun validateForm(): Boolean {

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

    private fun navigateToNextScreen() =
        findNavController().navigate(R.id.action_fragmentJobDetails_to_fragmentPaperDetails)


}