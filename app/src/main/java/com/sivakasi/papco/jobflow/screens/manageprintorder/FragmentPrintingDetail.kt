package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.PrintingDetail
import com.sivakasi.papco.jobflow.databinding.FragmentPrintingDetailBinding
import com.sivakasi.papco.jobflow.util.Duration
import com.sivakasi.papco.jobflow.util.FormValidator
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.validateForNonBlank
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentPrintingDetail : Fragment(), DialogRunningTime.DialogRunningTimeListener {

    private var _viewBinding: FragmentPrintingDetailBinding? = null
    private val viewBinding: FragmentPrintingDetailBinding
        get() = _viewBinding!!

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)

    private var duration: Duration = Duration()
    private var hasSpotColours: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentPrintingDetailBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
    }

    override fun onStop() {
        super.onStop()
        saveStateToViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        with(viewBinding) {
            txtColours.clearErrorOnTextChange()
            txtRunningTime.clearErrorOnTextChange()
        }

        viewBinding.txtRunningTime.setOnClickListener {
            showRunningTimeDialog()
        }

        viewBinding.btnNext.setOnClickListener {
            if (validateForm()) {
                saveStateToViewModel()
                findNavController().navigate(R.id.action_fragmentPrintingDetail_to_fragmentPostPressDetails)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loadedJob.observe(viewLifecycleOwner) {
            if (it is LoadingStatus.Success<*>)
                loadValues(it.data as PrintOrder)
        }
    }

    private fun loadValues(printOrder: PrintOrder) {
        val printingDetails = printOrder.printingDetail
        viewBinding.txtColours.setText(printingDetails.colours)
        viewBinding.txtPrintingDetails.setText(printingDetails.printingInstructions)
        loadRunningTime(
            Duration.fromMinutes(printingDetails.runningMinutes),
            printingDetails.hasSpotColours
        )

    }

    private fun loadRunningTime(runningTime: Duration, hasSpotColours: Boolean) {
        duration = runningTime
        this.hasSpotColours = hasSpotColours
        if (duration.inMinutes() > 0)
            viewBinding.txtRunningTime.setText(duration.toString())
        else
            viewBinding.txtRunningTime.setText("")
    }


    private fun showRunningTimeDialog() {
        DialogRunningTime.getInstance(duration, hasSpotColours).show(
            childFragmentManager,
            DialogRunningTime.TAG
        )
    }

    override fun onSubmitRunningTime(runningTime: Duration, hasSpotColours: Boolean) {
        loadRunningTime(runningTime, hasSpotColours)
    }

    private fun saveStateToViewModel() {
        val state = PrintingDetail()
        state.colours = viewBinding.txtColours.text.toString().trim()
        state.printingInstructions = viewBinding.txtPrintingDetails.text.toString()
        state.runningMinutes = duration.inMinutes()
        state.hasSpotColours = hasSpotColours
        viewModel.savePrintingDetails(state)
    }


    //Validation

    private fun validateForm(): Boolean =
        FormValidator()
            .validate(validateColours())
            .validate(validateRunningTime())
            .isValid()


    private fun validateColours(): Boolean =
        viewBinding.txtColours.validateForNonBlank(getString(R.string.required_field))


    private fun validateRunningTime(): Boolean {
        return if (duration.inMinutes() == 0) {
            viewBinding.layoutRunningTime.error = getString(R.string.error_not_optional_field)
            false
        } else
            true
    }


}