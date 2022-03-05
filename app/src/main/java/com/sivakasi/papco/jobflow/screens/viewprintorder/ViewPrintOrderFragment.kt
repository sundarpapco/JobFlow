package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.asDateString
import com.sivakasi.papco.jobflow.calendarWithTime
import com.sivakasi.papco.jobflow.common.hideWaitDialog
import com.sivakasi.papco.jobflow.common.showWaitDialog
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.databinding.FragmentViewPrintOrderBinding
import com.sivakasi.papco.jobflow.databinding.PaperDetailBinding
import com.sivakasi.papco.jobflow.databinding.PostPressDetailBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.print.PrintOrderAdapter
import com.sivakasi.papco.jobflow.print.PrintOrderReport
import com.sivakasi.papco.jobflow.screens.manageprintorder.FragmentAddPO
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewPrintOrderFragment : Fragment() {

    companion object {
        private const val KEY_DESTINATION_ID = "key:destination:id"
        private const val KEY_PO_ID = "key:printOrder:id"

        fun getArguments(destinationId: String, poId: String) =
            Bundle().apply {
                putString(KEY_DESTINATION_ID, destinationId)
                putString(KEY_PO_ID, poId)
            }
    }

    @Inject
    lateinit var printOrderReport: PrintOrderReport
    private lateinit var printOrder: PrintOrder
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
        if (currentUserRole() == "printer")
            setHasOptionsMenu(false)
        else
            setHasOptionsMenu(true)


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
        enableBackArrow()
        initViews()
        observerViewModel()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (currentUserRole() != "printer")
            inflater.inflate(R.menu.fragment_view_print_order, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.mnu_print -> {
                print()
                true
            }

            R.id.mnu_share_pdf -> {
                viewModel.generatePdfFile(printOrder)
                true
            }

            R.id.mnu_notes -> {
                navigateToNotesScreen()
                true
            }

            R.id.mnu_repeat_this_job -> {
                repeatThisJob()
                true
            }

            android.R.id.home -> findNavController().popBackStack()

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {

        viewBinding.fab.setOnClickListener {
            navigateToEditPrintOrderScreen()
        }

        //Disable editing of this print order if this it printer version of app or this print order
        //is completed

        val userRole = currentUserRole()

        if (getDestinationId() == DatabaseContract.DOCUMENT_DEST_COMPLETED) {
            if (userRole != "root")
                viewBinding.fab.hide()
        } else {
            if (userRole == "printer")
                viewBinding.fab.hide()
        }

       /* if (currentUserRole() == "printer" || getDestinationId() == DatabaseContract.DOCUMENT_DEST_COMPLETED)
            viewBinding.fab.hide()
        else
            viewBinding.fab.setOnClickListener {
                navigateToEditPrintOrderScreen()
            }*/
    }

    private fun observerViewModel() {
        viewModel.loadedPrintOrder.observe(viewLifecycleOwner) {
            handleLoadingStatus(it)
        }

        viewModel.generatePdfStatus.observe(viewLifecycleOwner, EventObserver {
            handleGeneratePdfStatus(it)
        })

        viewModel.destinationName.observe(viewLifecycleOwner) {
            updateSubTitle(it)
        }
    }

    private fun renderPrintOrder(printOrder: PrintOrder) {

        updateTitle(getString(R.string.po_xx, printOrder.printOrderNumber))
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

            if (printOrder.invoiceDetails.isNotBlank()) {
                poDetails.lblInvoiceNumber.visibility = View.VISIBLE
                poDetails.txtInvoiceNumber.visibility = View.VISIBLE
                poDetails.txtInvoiceNumber.text = printOrder.invoiceDetails
            } else {
                poDetails.lblInvoiceNumber.visibility = View.GONE
                poDetails.txtInvoiceNumber.visibility = View.GONE
            }
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
            lastPostPressDetail.findViewById<View>(R.id.separator).visibility = View.GONE
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
                printOrder = loadingState.data as PrintOrder
                renderPrintOrder(printOrder)
            }

            is LoadingStatus.Error -> {

                val exception = loadingState.exception
                if (exception is ResourceNotFoundException) {
                    showPrintOrderRemovedDialog()
                } else
                    toast(exception.message ?: getString(R.string.error_unknown_error))
            }

        }
    }

    private fun handleGeneratePdfStatus(loadingStatus: LoadingStatus) {

        when (loadingStatus) {
            is LoadingStatus.Loading -> {
                showWaitDialog(loadingStatus.msg)
            }
            is LoadingStatus.Success<*> -> {
                hideWaitDialog()
                sharePdfFile(loadingStatus.data as String)
            }
            is LoadingStatus.Error -> {
                hideWaitDialog()
                toast(loadingStatus.exception.message ?: getString(R.string.error_unknown_error))
            }
        }

    }

    private fun navigateToEditPrintOrderScreen() {
        findNavController().navigate(
            R.id.action_viewPrintOrderFragment_to_print_order_flow,
            FragmentAddPO.getArgumentBundle(printOrderNumber, getDestinationId())
        )
    }

    private fun repeatThisJob() {
        findNavController().navigate(
            R.id.action_viewPrintOrderFragment_to_print_order_flow,
            FragmentAddPO.getArgumentBundle(
                printOrder.printOrderNumber,
                DatabaseContract.DOCUMENT_DEST_NEW_JOBS,
                true
            )
        )
    }

    private fun print() {

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "PrintOrder"
        val printAdapter = PrintOrderAdapter(printOrder, printOrderReport)
        printManager.print(jobName, printAdapter, printAttributes)

    }

    private fun showPrintOrderRemovedDialog() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.po_not_found_desc))
        builder.setTitle(getString(R.string.po_not_found))
        builder.setPositiveButton(getString(R.string.exit)) { _, _ ->
            findNavController().popBackStack()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    private fun sharePdfFile(filePath: String) {
        requireContext().shareReport(filePath)
    }

    private fun navigateToNotesScreen() {
        findNavController().navigate(
            R.id.action_viewPrintOrderFragment_to_notesFragment,
            NotesFragment.getArguments(getDestinationId(), getPoId(), printOrder.notes)
        )
    }

    private fun getDestinationId(): String =
        arguments?.getString(KEY_DESTINATION_ID) ?: error("Destination Id argument not found")

    private fun getPoId(): String =
        arguments?.getString(KEY_PO_ID) ?: error("PO Id argument not found")
}