package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.common.ConfirmationDialog
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentAddPoBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import com.wajahatkarim3.easyvalidation.core.rules.GreaterThanOrEqualRule
import com.wajahatkarim3.easyvalidation.core.rules.ValidNumberRule
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentAddPO : Fragment(), ConfirmationDialog.ConfirmationDialogListener {

    companion object {
        private const val KEY_EDITING_PO_ID = "key:editing:po:id"
        private const val KEY_PARENT_DESTINATION_ID = "key:parent:destination"
        private const val KEY_ARG_AUTO_REPEAT = "key:auto:load"
        private const val KEY_SEARCH_MODE = "key:search:mode"
        private const val CONFIRMATION_RID_NOT_FOUND = 1

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

    private var _viewBinding: FragmentAddPoBinding? = null
    private val viewBinding: FragmentAddPoBinding
        get() = _viewBinding!!

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow) { defaultViewModelProviderFactory }
    private var searchByPlateNumber: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            searchByPlateNumber = it.getBoolean(KEY_SEARCH_MODE, true)
        }

        /*
        Auto repeat mode means that the user clicked "Repeat this job" in the
        menu of the ViewPrintOrder Screen. In this case, the printOrderNumber provided
        as argument will be used directly to create a reprint Job
        */
        if(isAutoRepeatMode()){
            check(getEditingPOId() > 0){"Invalid PO number provided in auto repeat mode"}
            //Search and load from repo using the provided PO number and not plate number
            viewModel.loadRepeatJob(getEditingPOId(),false)
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
        _viewBinding = FragmentAddPoBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackAsClose()

        if (isEditMode())
            updateTitle(getString(R.string.edit_job))
        else
            updateTitle(getString(R.string.create_job))
        updateSubTitle("")

        initViews()
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SEARCH_MODE, searchByPlateNumber)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        viewBinding.txtPlateNumber.clearErrorOnTextChange()
        viewBinding.btnSearch.setOnClickListener {
            toggleSearchMode()
        }

        viewBinding.btnNext.setOnClickListener {
            onNextPressed()
        }

        viewBinding.radioButtonNewJob.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                hideSearchLayout()
            } else {
                showSearchLayout()
            }
        }

        viewBinding.txtPlateNumber.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextPressed()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.reprintLoadingStatus.observe(viewLifecycleOwner, EventObserver {

            if (isAutoRepeatMode() || isEditMode())
                handleJobLoadInEditMode(it)
            else
                handleJobLoadInNonEditMode(it)

        })

        viewModel.loadedJob.observe(viewLifecycleOwner) {
            //A valid print order has been successfully loaded. So, navigate to next screen
            navigateToNextScreen()
        }
    }

    private fun hideSearchLayout() {
        viewBinding.layoutPlateNumber.visibility = View.GONE
        viewBinding.btnSearch.visibility = View.GONE
    }

    private fun showSearchLayout() {
        viewBinding.layoutPlateNumber.visibility = View.VISIBLE
        viewBinding.btnSearch.visibility = View.VISIBLE
    }

    private fun toggleSearchMode() {
        if (searchByPlateNumber) {
            searchByPlateNumber = false
            viewBinding.btnSearch.text = getString(R.string.po_number)
            viewBinding.layoutPlateNumber.hint = getString(R.string.po_number)
            viewBinding.layoutPlateNumber.helperText = getString(R.string.required_field)
        } else {
            searchByPlateNumber = true
            viewBinding.btnSearch.text = getString(R.string.rid)
            viewBinding.layoutPlateNumber.hint = getString(R.string.rid)
            viewBinding.layoutPlateNumber.helperText = getString(R.string.blank_if_party_plate)
        }
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

        viewModel.loadRepeatJob(plateNumber, searchByPlateNumber)

    }

    private fun validatePlateNumber(): Boolean {

        if (viewBinding.txtPlateNumber.text!!.isBlank() && searchByPlateNumber)
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

    private fun showPlateNumberNotFoundDialog() {

        val rid = viewBinding.txtPlateNumber.text.toString().trim()

        ConfirmationDialog.getInstance(
            getString(R.string.confirmation_old_print_order_not_found_proceed),
            getString(R.string.proceed),
            CONFIRMATION_RID_NOT_FOUND,
            getString(R.string.print_order_not_found),
            rid
        ).show(childFragmentManager, ConfirmationDialog.TAG)
    }

    private fun showPrintOrderNotFoundDialog() {
        ConfirmationDialog.getInstance(
            getString(R.string.error_po_not_found),
            getString(R.string.ok),
            2,
            getString(R.string.print_order_not_found)
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
        if (confirmationId == CONFIRMATION_RID_NOT_FOUND)
            viewModel.createRepeatJob(extra.toInt())
    }

    private fun handleJobLoadInEditMode(loadingStatus: LoadingStatus) {

        when (loadingStatus) {
            is LoadingStatus.Loading -> {
                viewBinding.fullscreenProgressBar.root.visibility = View.VISIBLE
            }

            is LoadingStatus.Success<*> -> {
                viewModel.saveLoadedJob(loadingStatus.data as PrintOrder)
            }

            is LoadingStatus.Error -> {
                viewBinding.fullscreenProgressBar.root.visibility = View.GONE
                toast(loadingStatus.exception.message ?: getString(R.string.error_unknown_error))
                findNavController().popBackStack()
            }

        }

    }

    private fun handleJobLoadInNonEditMode(loadingStatus: LoadingStatus) {

        when (loadingStatus) {
            is LoadingStatus.Loading -> {
                renderLoadingState(loadingStatus.msg)
            }

            is LoadingStatus.Success<*> -> {
                viewModel.saveLoadedJob(loadingStatus.data as PrintOrder)
            }

            is LoadingStatus.Error -> {
                hideLoadingState()
                when (loadingStatus.exception) {
                    is ResourceNotFoundException -> {
                        if (searchByPlateNumber)
                            showPlateNumberNotFoundDialog()
                        else
                            showPrintOrderNotFoundDialog()
                    }

                    is IllegalArgumentException -> {
                        showUnExpectedError(getString(R.string.invalid_rid_entered))
                    }

                    else -> showUnExpectedError(
                        loadingStatus.exception.message ?: getString(R.string.error_unknown_error)
                    )
                }
            }

        }

    }

    private fun isEditMode(): Boolean = getEditingPOId() != -4

    private fun isAutoRepeatMode(): Boolean =
        arguments?.getBoolean(KEY_ARG_AUTO_REPEAT) ?: false

    private fun getParentDestinationId(): String =
        arguments?.getString(KEY_PARENT_DESTINATION_ID) ?: DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    private fun getEditingPOId(): Int =
        arguments?.getInt(KEY_EDITING_PO_ID) ?: -4
}