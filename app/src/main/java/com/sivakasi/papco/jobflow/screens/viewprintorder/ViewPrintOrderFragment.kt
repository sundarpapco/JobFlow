package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.asDateString
import com.sivakasi.papco.jobflow.calendarWithTime
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.databinding.FragmentViewPrintOrderBinding
import com.sivakasi.papco.jobflow.databinding.PaperDetailBinding
import com.sivakasi.papco.jobflow.databinding.PostPressDetailBinding
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.screens.manageprintorder.FragmentAddPO
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import com.sivakasi.papco.jobflow.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewPrintOrderFragment : Fragment() {

    companion object {
        private const val KEY_DESTINATION_ID = "key:destination:id"
        private const val KEY_DESTINATION_TYPE = "key:destination:type"
        private const val KEY_PO_ID = "key:printOrder:id"

        fun getArguments(destinationId: String, destinationType: Int, poId: String) =
            Bundle().apply {
                putInt(KEY_DESTINATION_TYPE, destinationType)
                putString(KEY_DESTINATION_ID, destinationId)
                putString(KEY_PO_ID, poId)
            }
    }

    private var printOrderNumber: Int = -1
    private var _viewBinding: FragmentViewPrintOrderBinding? = null
    private val viewBinding: FragmentViewPrintOrderBinding
        get() = _viewBinding!!
    private val viewModel by lazy {
        ViewModelProvider(this).get(ViewPrintOrderVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadPrintOrder(getDestinationId(), getPoId())
        if (getDestinationType() == Destination.TYPE_FIXED)
            setHasOptionsMenu(true)
        else
            setHasOptionsMenu(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _viewBinding = FragmentViewPrintOrderBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observerViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_view_print_order, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.mnu_edit_po) {
            navigateToEditPrintOrderScreen()
            return true
        }

        if (item.itemId == R.id.mnu_print) {
            toast("Print the PO here")
            return true
        }

        return false

    }

    private fun observerViewModel() {
        viewModel.loadedPrintOrder.observe(viewLifecycleOwner) {
            handleLoadingStatus(it)
        }
    }

    private fun renderPrintOrder(printOrder: PrintOrder) {

        updateTitle(getString(R.string.po_xx, printOrder.printOrderNumber))
        updateSubTitle("")
        printOrderNumber = printOrder.printOrderNumber

        renderPoDetails(printOrder)
        renderPlateMakingDetail(printOrder.plateMakingDetail)
        renderPaperDetails(printOrder.paperDetails!!)
        renderPrintingDetails(printOrder.printingDetail)
        renderPostPressDetails(printOrder)

    }

    private fun renderPoDetails(printOrder: PrintOrder) {
        with(viewBinding) {
            poDetails.txtDate.text = calendarWithTime(printOrder.creationTime).asDateString()
            poDetails.txtClientName.text = printOrder.billingName
            poDetails.txtJobName.text = printOrder.jobName
            val paperDetail = printOrder.printingSizePaperDetail()
            poDetails.txtPrintingSize.text = paperDetail.paperSize()
            poDetails.txtPrintingQuantity.text = getString(R.string.xx_sheets, paperDetail.sheets)
            poDetails.txtJobType.text = if (printOrder.jobType == PrintOrder.TYPE_NEW_JOB)
                getString(R.string.new_job)
            else
                getString(R.string.repeat_job)
        }
    }

    private fun renderPlateMakingDetail(plateDetails: PlateMakingDetail) {

        with(viewBinding.plateMakingDetails) {
            txtPlateNumber.text =
                if (plateDetails.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                    getString(R.string.outside_plate)
                else
                    plateDetails.plateNumber.toString()
            txtTrimmingSize.text = plateDetails.trimmingSize
            txtJobSize.text = plateDetails.jobSize
            txtGripper.text = plateDetails.gripperSize
            txtTail.text = plateDetails.tailSize
            txtMachine.text = plateDetails.machine
            txtScreen.text = plateDetails.screen
            txtBackside.text = plateDetails.backsidePrinting
            txtBacksideMachine.text = plateDetails.backsideMachine
        }

    }

    private fun renderPaperDetails(paperDetails: List<PaperDetail>) {

        with(viewBinding.paperDetails) {

            removeAllViews()
            var paperDetail: PaperDetailBinding
            paperDetails.forEachIndexed { index, paper ->

                paperDetail = PaperDetailBinding.inflate(layoutInflater, this, false)
                paperDetail.txtPaperOwner.text = if (paper.partyPaper)
                    getString(R.string.party_own)
                else
                    getString(R.string.our_own)
                paperDetail.txtSheets.text = getString(R.string.xx_sheets, paper.sheets)
                paperDetail.txtPaperName.text = paper.paperName()

                addView(paperDetail.root)

                if (index == paperDetails.size - 1)
                    paperDetail.separator.visibility = View.GONE
            }

        }

    }

    private fun renderPrintingDetails(printingDetail: PrintingDetail) {

        with(viewBinding.printingDetails) {

            txtColors.text = printingDetail.colours
            if (printingDetail.printingInstructions.isNotBlank()) {
                txtPrintingDetail.visibility = View.VISIBLE
                txtPrintingDetail.text = printingDetail.printingInstructions
            } else {
                txtPrintingDetail.visibility = View.GONE
                separator.visibility = View.GONE
            }

        }

    }

    private fun renderPostPressDetails(printOrder: PrintOrder) {

        viewBinding.postPressDetails.visibility = View.VISIBLE
        viewBinding.lblHeadingPostPressDetails.visibility = View.VISIBLE

        viewBinding.postPressDetails.removeAllViews()
        var postPressBinding: PostPressDetailBinding
        printOrder.lamination?.let {
            postPressBinding =
                PostPressDetailBinding.inflate(layoutInflater, viewBinding.postPressDetails, false)
            postPressBinding.name.text = getString(R.string.lamination)
            postPressBinding.details.text = it.toString()
            if (it.remarks.isNotBlank())
                postPressBinding.remarks.text = it.remarks
            else
                postPressBinding.remarks.visibility = View.GONE
            viewBinding.postPressDetails.addView(postPressBinding.root)
        }

        printOrder.foil?.let {
            renderSimplePostPressOperation(getString(R.string.foils), it)
        }

        printOrder.scoring?.let {
            renderSimplePostPressOperation(getString(R.string.scoring), it)
        }

        printOrder.folding?.let {
            renderSimplePostPressOperation(getString(R.string.folding), it)
        }

        printOrder.binding?.let {
            postPressBinding =
                PostPressDetailBinding.inflate(layoutInflater, viewBinding.postPressDetails, false)
            postPressBinding.name.text = getString(R.string.binding)
            postPressBinding.details.text = it.getBindingName(requireContext())
            if (it.remarks.isNotBlank())
                postPressBinding.remarks.text = it.remarks
            else
                postPressBinding.remarks.visibility = View.GONE
            viewBinding.postPressDetails.addView(postPressBinding.root)
        }

        printOrder.spotUV?.let {
            renderSimplePostPressOperation(getString(R.string.spot_uv), it)
        }

        printOrder.aqueousCoating?.let {
            renderSimplePostPressOperation(getString(R.string.aqueous_coating), it)
        }

        printOrder.cutting?.let {
            renderSimplePostPressOperation(getString(R.string.cutting), it)
        }

        printOrder.packing?.let {
            renderSimplePostPressOperation(getString(R.string.packing), it)
        }

        if (viewBinding.postPressDetails.childCount == 0) {
            viewBinding.postPressDetails.visibility = View.GONE
            viewBinding.lblHeadingPostPressDetails.visibility = View.GONE
        } else {
            val lastPostPressDetail =
                viewBinding.postPressDetails[viewBinding.postPressDetails.childCount - 1]
            lastPostPressDetail.findViewById<View>(R.id.separator).visibility=View.GONE
        }

    }

    private fun renderSimplePostPressOperation(name: String, remarks: String) {
        val postPressBinding =
            PostPressDetailBinding.inflate(layoutInflater, viewBinding.postPressDetails, false)
        postPressBinding.name.text = name
        postPressBinding.details.visibility = View.GONE
        if (remarks.isNotBlank())
            postPressBinding.remarks.text = remarks
        else
            postPressBinding.remarks.visibility = View.GONE

        viewBinding.postPressDetails.addView(postPressBinding.root)
    }

    private fun handleLoadingStatus(loadingState: LoadingStatus) {

        when (loadingState) {

            is LoadingStatus.Loading -> {

            }

            is LoadingStatus.Success<*> -> {
                val printOrder = loadingState.data as PrintOrder
                renderPrintOrder(printOrder)
            }

            is LoadingStatus.Error -> {

                val exception = loadingState.exception
                if (exception is ResourceNotFoundException) {
                    toast(getString(R.string.print_order_not_found))
                    findNavController().popBackStack()
                } else
                    toast(exception.message ?: getString(R.string.error_unknown_error))
            }

        }
    }

    private fun navigateToEditPrintOrderScreen() {
        findNavController().navigate(
            R.id.action_viewPrintOrderFragment_to_print_order_flow,
            FragmentAddPO.getArgumentBundle(printOrderNumber)
        )
    }

    private fun getDestinationId(): String =
        arguments?.getString(KEY_DESTINATION_ID) ?: error("Destination Id argument not found")

    private fun getPoId(): String =
        arguments?.getString(KEY_PO_ID) ?: error("PO Id argument not found")

    private fun getDestinationType(): Int =
        arguments?.getInt(KEY_DESTINATION_TYPE) ?: error("Destination type argument not found")
}