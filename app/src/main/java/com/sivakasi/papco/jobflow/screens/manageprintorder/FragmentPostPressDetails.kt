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
import com.sivakasi.papco.jobflow.common.DialogTextInput
import com.sivakasi.papco.jobflow.common.ResultDialogFragment
import com.sivakasi.papco.jobflow.common.hideWaitDialog
import com.sivakasi.papco.jobflow.common.showWaitDialog
import com.sivakasi.papco.jobflow.data.Binding
import com.sivakasi.papco.jobflow.data.Lamination
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentPostPressDetailsBinding
import com.sivakasi.papco.jobflow.extensions.enableBackAsClose
import com.sivakasi.papco.jobflow.extensions.toast
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentPostPressDetails : Fragment(), ResultDialogFragment.ResultDialogListener {

    companion object {
        private const val DIALOG_CODE_LAMINATION = 1
        private const val DIALOG_CODE_SCORING = 2
        private const val DIALOG_CODE_FOLDING = 3
        private const val DIALOG_CODE_SPOT_UV = 4
        private const val DIALOG_CODE_AQUEOUS_COATING = 5
        private const val DIALOG_CODE_CUTTING = 6
        private const val DIALOG_CODE_PACKING = 7
        private const val DIALOG_CODE_FOILS = 8
        private const val DIALOG_CODE_BINDING = 9
    }

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)
    private var _viewBinding: FragmentPostPressDetailsBinding? = null
    private val viewBinding get() = _viewBinding!!
    private var printOrder: PrintOrder? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentPostPressDetailsBinding.inflate(inflater, container, false)
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            exitOutOfCreationFlow()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        if(viewModel.isEditMode)
            viewBinding.btnDone.text=getString(R.string.save_print_order)

        viewBinding.btnDone.setOnClickListener {
            if (viewModel.isEditMode)
                viewModel.updatePrintOrder()
            else
                viewModel.savePrintOrder()
        }

        viewBinding.chkboxLamination.setOnClickListener {
            if (viewBinding.chkboxLamination.isChecked) {
                //User is enabling the lamination
                printOrder?.lamination = Lamination()
                showLaminationDialog()
            } else {
                //User is disabling the lamination
                printOrder?.lamination = null
            }
            renderLamination()
        }

        viewBinding.chkboxScoring.setOnClickListener {
            if (viewBinding.chkboxScoring.isChecked) {
                //User is enabling scoring
                printOrder?.scoring = ""
                showRemarksDialog(getString(R.string.scoring), DIALOG_CODE_SCORING)
            } else {
                //User is disabling scoring
                printOrder?.scoring = null
            }
            renderScoring()
        }

        viewBinding.chkboxFolding.setOnClickListener {
            if (viewBinding.chkboxFolding.isChecked) {
                //User is enabling scoring
                printOrder?.folding = ""
                showRemarksDialog(getString(R.string.folding), DIALOG_CODE_FOLDING)
            } else {
                //User is disabling scoring
                printOrder?.folding = null
            }
            renderFolding()
        }

        viewBinding.chkboxSpotUv.setOnClickListener {
            if (viewBinding.chkboxSpotUv.isChecked) {
                //User is enabling scoring
                printOrder?.spotUV = ""
                showRemarksDialog(getString(R.string.spot_uv), DIALOG_CODE_SPOT_UV)
            } else {
                //User is disabling scoring
                printOrder?.spotUV = null
            }
            renderSpotUV()
        }

        viewBinding.chkboxAqueousCoating.setOnClickListener {
            if (viewBinding.chkboxAqueousCoating.isChecked) {
                //User is enabling this operation
                printOrder?.aqueousCoating = ""
                showRemarksDialog(getString(R.string.aqueous_coating), DIALOG_CODE_AQUEOUS_COATING)
            } else {
                //User is disabling this operation
                printOrder?.aqueousCoating = null
            }
            renderCoating()
        }

        viewBinding.chkboxCutting.setOnClickListener {
            if (viewBinding.chkboxCutting.isChecked) {
                //User is enabling this operation
                printOrder?.cutting = ""
                showRemarksDialog(getString(R.string.cutting), DIALOG_CODE_CUTTING)
            } else {
                //User is disabling this operation
                printOrder?.cutting = null
            }
            renderCutting()
        }

        viewBinding.chkboxPacking.setOnClickListener {
            if (viewBinding.chkboxPacking.isChecked) {
                //User is enabling this operation
                printOrder?.packing = ""
                showRemarksDialog(getString(R.string.packing), DIALOG_CODE_PACKING)
            } else {
                //User is disabling this operation
                printOrder?.packing = null
            }
            renderPacking()
        }

        viewBinding.chkboxFoils.setOnClickListener {
            if (viewBinding.chkboxFoils.isChecked) {
                //User is enabling this operation
                printOrder?.foil = ""
                showRemarksDialog(getString(R.string.foils), DIALOG_CODE_FOILS)
            } else {
                //User is disabling this operation
                printOrder?.foil = null
            }
            renderFoils()
        }

        viewBinding.chkboxBinding.setOnClickListener {
            if (viewBinding.chkboxBinding.isChecked) {
                //User is enabling the lamination
                printOrder?.binding = Binding()
                showBindingDialog()
            } else {
                //User is disabling the lamination
                printOrder?.binding = null
            }
            renderBinding()
        }

    }

    private fun observeViewModel() {
        viewModel.loadedJob.observe(viewLifecycleOwner) {
                printOrder = it
                renderScreen()
        }

        viewModel.saveStatus.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is LoadingStatus.Loading -> showWaitDialog(it.msg)

                is LoadingStatus.Success<*> -> {
                    hideWaitDialog()
                    if (viewModel.isEditMode)
                        toast(getString(R.string.success_print_order_updating))
                    else
                        toast(getString(R.string.success_print_order_creation))

                    exitOutOfCreationFlow()
                }

                is LoadingStatus.Error -> {
                    hideWaitDialog()
                    toast(it.exception.message ?: "Unknown error")
                }
            }
        })
    }

    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }

    private fun showLaminationDialog() {
        DialogLamination.getInstance(printOrder?.lamination, DIALOG_CODE_LAMINATION).show(
            childFragmentManager, DialogLamination.TAG
        )
    }

    private fun showBindingDialog() {
        DialogBinding.getInstance(code = DIALOG_CODE_BINDING).show(
            childFragmentManager, DialogBinding.TAG
        )
    }

    private fun showRemarksDialog(title: String, code: Int) {
        DialogTextInput.getInstance(
            title,
            "",
            code
        ).show(childFragmentManager, DialogTextInput.TAG)
    }


    override fun onDialogResult(dialogResult: Any, code: Int) {

        when (code) {

            DIALOG_CODE_LAMINATION -> {
                printOrder?.lamination = dialogResult as Lamination
                renderLamination()
            }

            DIALOG_CODE_SCORING -> {
                printOrder?.scoring = dialogResult as String
                renderScoring()
            }

            DIALOG_CODE_FOLDING -> {
                printOrder?.folding = dialogResult as String
                renderFolding()
            }

            DIALOG_CODE_SPOT_UV -> {
                printOrder?.spotUV = dialogResult as String
                renderSpotUV()
            }

            DIALOG_CODE_AQUEOUS_COATING -> {
                printOrder?.aqueousCoating = dialogResult as String
                renderCoating()
            }

            DIALOG_CODE_CUTTING -> {
                printOrder?.cutting = dialogResult as String
                renderCutting()
            }

            DIALOG_CODE_PACKING -> {
                printOrder?.packing = dialogResult as String
                renderPacking()
            }

            DIALOG_CODE_FOILS -> {
                printOrder?.foil = dialogResult as String
                renderFoils()
            }

            DIALOG_CODE_BINDING -> {
                printOrder?.binding = dialogResult as Binding
                renderBinding()
            }
        }

    }

    private fun renderScreen() {
        renderLamination()
        renderScoring()
        renderFolding()
        renderSpotUV()
        renderCoating()
        renderCutting()
        renderPacking()
        renderFoils()
        renderBinding()
    }

    private fun renderLamination() {
        printOrder?.lamination?.let {
            viewBinding.chkboxLamination.isChecked = true
            viewBinding.lblLaminationDetail1.visibility = View.VISIBLE
            viewBinding.lblLaminationDetail1.text = it.toString()
            if (it.remarks.isBlank()) {
                viewBinding.lblLaminationDetail2.visibility = View.GONE
            } else {
                viewBinding.lblLaminationDetail2.visibility = View.VISIBLE
                viewBinding.lblLaminationDetail2.text = it.remarks
            }
        } ?: run {
            viewBinding.chkboxLamination.isChecked = false
            viewBinding.lblLaminationDetail1.visibility = View.GONE
            viewBinding.lblLaminationDetail2.visibility = View.GONE
        }
    }

    private fun renderScoring() {
        printOrder?.scoring?.let {
            viewBinding.chkboxScoring.isChecked = true
            if (it.isBlank())
                viewBinding.lblScoringDetail1.visibility = View.GONE
            else {
                viewBinding.lblScoringDetail1.visibility = View.VISIBLE
                viewBinding.lblScoringDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxScoring.isChecked = false
            viewBinding.lblScoringDetail1.visibility = View.GONE
        }
    }

    private fun renderFolding() {
        printOrder?.folding?.let {
            viewBinding.chkboxFolding.isChecked = true
            if (it.isBlank())
                viewBinding.lblFoldingDetail1.visibility = View.GONE
            else {
                viewBinding.lblFoldingDetail1.visibility = View.VISIBLE
                viewBinding.lblFoldingDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxFolding.isChecked = false
            viewBinding.lblFoldingDetail1.visibility = View.GONE
        }
    }

    private fun renderSpotUV() {
        printOrder?.spotUV?.let {
            viewBinding.chkboxSpotUv.isChecked = true
            if (it.isBlank())
                viewBinding.lblSpotUvDetail1.visibility = View.GONE
            else {
                viewBinding.lblSpotUvDetail1.visibility = View.VISIBLE
                viewBinding.lblSpotUvDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxSpotUv.isChecked = false
            viewBinding.lblSpotUvDetail1.visibility = View.GONE
        }
    }

    private fun renderCoating() {
        printOrder?.aqueousCoating?.let {
            viewBinding.chkboxAqueousCoating.isChecked = true
            if (it.isBlank())
                viewBinding.lblAqueousCoatingDetail1.visibility = View.GONE
            else {
                viewBinding.lblAqueousCoatingDetail1.visibility = View.VISIBLE
                viewBinding.lblAqueousCoatingDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxAqueousCoating.isChecked = false
            viewBinding.lblAqueousCoatingDetail1.visibility = View.GONE
        }
    }

    private fun renderCutting() {
        printOrder?.cutting?.let {
            viewBinding.chkboxCutting.isChecked = true
            if (it.isBlank())
                viewBinding.lblCuttingDetail1.visibility = View.GONE
            else {
                viewBinding.lblCuttingDetail1.visibility = View.VISIBLE
                viewBinding.lblCuttingDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxCutting.isChecked = false
            viewBinding.lblCuttingDetail1.visibility = View.GONE
        }
    }

    private fun renderPacking() {
        printOrder?.packing?.let {
            viewBinding.chkboxPacking.isChecked = true
            if (it.isBlank())
                viewBinding.lblPackingDetail1.visibility = View.GONE
            else {
                viewBinding.lblPackingDetail1.visibility = View.VISIBLE
                viewBinding.lblPackingDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxPacking.isChecked = false
            viewBinding.lblPackingDetail1.visibility = View.GONE
        }
    }

    private fun renderFoils() {
        printOrder?.foil?.let {
            viewBinding.chkboxFoils.isChecked = true
            if (it.isBlank())
                viewBinding.lblFoilsDetail1.visibility = View.GONE
            else {
                viewBinding.lblFoilsDetail1.visibility = View.VISIBLE
                viewBinding.lblFoilsDetail1.text = it
            }
        } ?: run {
            viewBinding.chkboxFoils.isChecked = false
            viewBinding.lblFoilsDetail1.visibility = View.GONE
        }
    }

    private fun renderBinding() {

        printOrder?.binding?.let {
            viewBinding.chkboxBinding.isChecked = true
            viewBinding.lblBindingDetail1.visibility = View.VISIBLE
            viewBinding.lblBindingDetail1.text = it.getBindingName(requireContext())
            if (it.remarks.isBlank())
                viewBinding.lblBindingDetail2.visibility = View.GONE
            else {
                viewBinding.lblBindingDetail2.text = it.remarks
                viewBinding.lblBindingDetail2.visibility = View.VISIBLE
            }
        } ?: run {
            viewBinding.chkboxBinding.isChecked = false
            viewBinding.lblBindingDetail1.visibility = View.GONE
            viewBinding.lblBindingDetail2.visibility = View.GONE
        }
    }


}