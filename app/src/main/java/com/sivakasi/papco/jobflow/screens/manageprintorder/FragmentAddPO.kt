package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.common.ConfirmationDialog
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.databinding.FragmentAddPoBinding
import com.sivakasi.papco.jobflow.extensions.number
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import com.sivakasi.papco.jobflow.util.toast
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.rules.ValidNumberRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentAddPO : Fragment(), ConfirmationDialog.ConfirmationDialogListener {

    companion object {
        private const val KEY_EDITING_PO_ID = "key:editing:po:id"
        private const val KEY_PARENT_DESTINATION_ID="key:parent:destination"

        fun getArgumentBundle(editingPONumber: Int,parentDestinationId:String): Bundle = Bundle().apply {
            putInt(KEY_EDITING_PO_ID, editingPONumber)
            putString(KEY_PARENT_DESTINATION_ID,parentDestinationId)
        }
    }

    private var _viewBinding: FragmentAddPoBinding? = null
    private val viewBinding: FragmentAddPoBinding
        get() = _viewBinding!!

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isEditMode()) {
            viewModel.isEditMode=true
            viewModel.editingPrintOrderParentDestinationId=getParentDestinationId()
            viewModel.loadPrintOrderToEdit(getEditingPOId())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentAddPoBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        viewBinding.txtPlateNumber.clearErrorOnTextChange()

        viewBinding.btnNext.setOnClickListener {
            onNextPressed()
        }

        viewBinding.radioButtonNewJob.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                hidePlateNumberLayout()
            } else {
                showPlateNumberLayout()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loadedJob.observe(viewLifecycleOwner) {
           if(isEditMode())
               handleJobLoadInEditMode(it)
            else
                handleJobLoadInNonEditMode(it)

        }
    }

    private fun hidePlateNumberLayout() {
        viewBinding.layoutPlateNumber.visibility = View.GONE
    }

    private fun showPlateNumberLayout() {
        viewBinding.layoutPlateNumber.visibility = View.VISIBLE
    }

    private fun onNextPressed() {

        if (isNewJob()) {
            viewModel.createNewJob()
            return
        }

        if (!validatePlateNumber())
            return

        val plateNumber =
            viewBinding.txtPlateNumber.number(PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)

        viewModel.loadRepeatJob(plateNumber)

    }

    private fun validatePlateNumber(): Boolean {

        if (viewBinding.txtPlateNumber.text!!.isBlank())
            return true

        return viewBinding.txtPlateNumber.validator()
            .addRule(ValidNumberRule())
            .addRule(GreaterThanOrEqualRule(1))
            .addErrorCallback {
                viewBinding.txtPlateNumber.error = getString(R.string.invalid_plate_number)
            }
            .check()
    }

    private fun renderLoadingState(msg: String) {
        viewBinding.progressBar.visibility = View.VISIBLE
        viewBinding.progressText.visibility = View.VISIBLE
        viewBinding.progressText.text = msg
        viewBinding.btnNext.isEnabled = false
    }

    private fun hideLoadingState() {
        viewBinding.progressBar.visibility = View.GONE
        viewBinding.progressText.visibility = View.GONE
        viewBinding.btnNext.isEnabled = true
    }

    private fun showPrintOrderNotFoundDialog() {

        val rid = viewBinding.txtPlateNumber.text.toString().trim()

        ConfirmationDialog.getInstance(
            getString(R.string.confirmation_old_print_order_not_found_proceed),
            getString(R.string.proceed),
            1,
            getString(R.string.print_order_not_found),
            rid
        ).show(childFragmentManager, ConfirmationDialog.TAG)
    }

    private fun showUnExpectedError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        viewBinding.btnNext.isEnabled = true
    }


    private fun isNewJob() =
        viewBinding.radioButtonNewJob.isChecked

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.action_fragmentAddPO_to_fragmentJobDetails)
    }

    override fun onConfirmationDialogConfirm(confirmationId: Int, extra: String) {
        viewModel.createRepeatJob(extra.toInt())
    }

    private fun handleJobLoadInEditMode(loadingStatus: LoadingStatus){

        when (loadingStatus) {
            is LoadingStatus.Loading -> {
                viewBinding.fullscreenProgressBar.root.visibility=View.VISIBLE
                renderLoadingState(loadingStatus.msg)
            }

            is LoadingStatus.Success<*> -> {
                navigateToNextScreen()
            }

            is LoadingStatus.Error -> {
                viewBinding.fullscreenProgressBar.root.visibility=View.GONE
                toast(loadingStatus.exception.message ?: getString(R.string.error_unknown_error))
                findNavController().popBackStack()
            }

        }

    }

    private fun handleJobLoadInNonEditMode(loadingStatus: LoadingStatus){

        when (loadingStatus) {
            is LoadingStatus.Loading -> {
                renderLoadingState(loadingStatus.msg)
            }

            is LoadingStatus.Success<*> -> {
                navigateToNextScreen()
            }

            is LoadingStatus.Error -> {
                hideLoadingState()
                if (loadingStatus.exception is ResourceNotFoundException)
                    showPrintOrderNotFoundDialog()
                else
                    showUnExpectedError(
                        loadingStatus.exception.message ?: getString(R.string.error_unknown_error)
                    )
            }

        }

    }

    private fun isEditMode(): Boolean = getEditingPOId() != -4

    private fun getParentDestinationId():String=
        arguments?.getString(KEY_PARENT_DESTINATION_ID) ?: DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    private fun getEditingPOId(): Int =
        arguments?.getInt(KEY_EDITING_PO_ID) ?: -4
}