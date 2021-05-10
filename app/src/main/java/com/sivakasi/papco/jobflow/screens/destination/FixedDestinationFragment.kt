package com.sivakasi.papco.jobflow.screens.destination

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.*
import com.sivakasi.papco.jobflow.currentTimeInMillis
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.databinding.DestinationFixedBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FixedDestinationFragment : Fragment(),
    JobsAdapterListener,
    ActionMode.Callback,
    ResultDialogFragment.ResultDialogListener,
    ConfirmationDialog.ConfirmationDialogListener {

    companion object {
        private const val KEY_DESTINATION_ID = "key:destination:id"
        private const val KEY_DESTINATION_TYPE = "key:destination:type"
        private const val DIALOG_CODE_INVOICE_DETAIL = 1
        private const val DIALOG_CODE_PENDING_REMARKS = 2

        private const val CONFIRMATION_CANCEL_JOBS = 1
        private const val CONFIRMATION_COMPLETE_JOBS = 2
        private const val CONFIRMATION_REVERT_JOBS = 3

        fun getArgumentBundle(destinationId: String, destinationType: Int): Bundle =
            Bundle().apply {
                putInt(KEY_DESTINATION_TYPE, destinationType)
                putString(KEY_DESTINATION_ID, destinationId)
            }
    }

    //Variable used to determine whether we should notifyDataSetChanged on the adapter when the action
    //mode finishes. When the action mode is finishing, this variable determines whether we should
    //refresh the adapter or not
    //True when the ActionMode is finishing cause of back arrow key and false when the action mode
    //is finishing as the result of allocation completion
    private var refreshAdapterNeeded: Boolean = true
    private var clearSelections: Boolean = true
    private var actionMode: ActionMode? = null
    private lateinit var selections: JobListSelection
    private var _viewBinding: DestinationFixedBinding? = null
    private val viewBinding: DestinationFixedBinding
        get() = _viewBinding!!


    private val viewModel: FixedDestinationVM by lazy {
        ViewModelProvider(this).get(FixedDestinationVM::class.java)
    }


    private val adapter: JobsAdapter by lazy {
        JobsAdapter(requireContext(), viewModel.jobSelections, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadJobsFromDestination(getDestinationId())
        selections = viewModel.jobSelections
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DestinationFixedBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
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

    override fun onDestroyView() {
        super.onDestroyView()
        //Clear the action mode without clearing the selection
        if (actionMode != null) {
            clearSelections = false
            actionMode?.finish()
        }
        actionMode = null
        adapter.itemTouchHelper = null
        viewBinding.recycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {
        viewBinding.fab.setOnClickListener {
            navigateToCreatePOScreen()
        }

        if (getDestinationId() != DatabaseContract.DOCUMENT_DEST_NEW_JOBS) {
            viewBinding.fab.setOnClickListener(null)
            viewBinding.fab.hide()
        }
        initRecycler()
    }

    private fun initRecycler() {

        val touchHelper = ItemTouchHelper(ItemTouchHelperCallBack(adapter))
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter.itemTouchHelper = touchHelper
        viewBinding.recycler.adapter = adapter
        touchHelper.attachToRecyclerView(viewBinding.recycler)
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        viewModel.loadedJobs.observe(viewLifecycleOwner) {

            when (it) {

                is LoadingStatus.Loading -> {
                    showProgressBar()
                }

                is LoadingStatus.Error -> {
                    hideProgress()
                    toast(it.exception.message ?: getString(R.string.error_unknown_error))
                }

                is LoadingStatus.Success<*> -> {
                    hideProgress()
                    adapter.submitList(it.data as List<PrintOrderUIModel>)
                }
            }
        }

        viewModel.jobSelections.updates.observe(viewLifecycleOwner) { selectionSize ->
            onSelectionChange(selectionSize)
        }

        viewModel.destination.observe(viewLifecycleOwner) {
            renderTitle(it)
        }

        viewModel.workingStatus.observe(viewLifecycleOwner, EventObserver {
            handleWorkingStatusEvent(it)
        })

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            ManageMachinesFragment.KEY_SELECTED_MACHINE_ID
        )?.observe(viewLifecycleOwner) {

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>(
                ManageMachinesFragment.KEY_SELECTED_MACHINE_ID
            )

            viewModel.allotSelectedJobs(getDestinationId(), it)
        }
    }

    private fun navigateToCreatePOScreen() {
        findNavController().navigate(R.id.action_fixedDestinationFragment_to_print_order_flow)
    }

    private fun showProgressBar() {
        viewBinding.progressBar.root.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        viewBinding.progressBar.root.visibility = View.GONE
    }

    //Callbacks for recycler view

    override fun onItemClick(item: PrintOrderUIModel, position: Int) {
        if (actionMode == null)
            navigateToViewPrintOrderScreen(item.documentId())
        //navigateToEditPrintOrderScreen(item.printOrderNumber)
        else {
            viewModel.jobSelections.toggle(item)
            adapter.notifyItemChanged(position)
        }
    }

    override fun onItemMoved(updatingJobs: List<PrintOrderUIModel>) {
        viewModel.updateJobs(getDestinationId(), updatingJobs)
    }

    override fun onItemLongClick(item: PrintOrderUIModel, position: Int) {
        //Start the action mode is its not already on
        viewModel.jobSelections.toggle(item)
        adapter.notifyItemChanged(position)

    }

    override fun showPendingRemarks(item: PrintOrderUIModel) {

        Snackbar.make(viewBinding.fab, item.pendingReason, Snackbar.LENGTH_LONG)
            .setAction(R.string.clear) {
                viewModel.clearPendingStatus(getDestinationId(), item)
            }
            .show()
    }

    // ----------------------------

    private fun onSelectionChange(selectionCount: Int) {

        if (selectionCount > 0) {
            if (actionMode == null) {
                val activity = requireActivity() as AppCompatActivity
                //SafeActionModeWrapper class used to prevent memory leak. See that class description comments
                actionMode = activity.startSupportActionMode(SafeActionModeCallBack(this))
            }

            actionMode?.title = viewModel.jobSelections.title()
            actionMode?.subtitle = viewModel.jobSelections.subTitle()
            actionMode?.invalidate()
        } else {
            actionMode?.finish()
            if (refreshAdapterNeeded) {
                adapter.notifyDataSetChanged()
            } else
                refreshAdapterNeeded = true
        }

    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        actionMode = mode
        if (getDestinationType() == Destination.TYPE_FIXED)
            mode?.menuInflater?.inflate(R.menu.action_fixed_destination, menu)
        else
            mode?.menuInflater?.inflate(R.menu.action_dynamic_destination, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {

        if (getDestinationType() == Destination.TYPE_DYNAMIC)
            return true

        menu?.let {

            val itemMark = it.findItem(R.id.mnu_mark_as_pending)
            val itemClear = it.findItem(R.id.mnu_clear_pending)
            if (selections.hasPendingItems()) {
                itemMark.isVisible = false
                itemClear.isVisible = true
            } else {
                itemMark.isVisible = true
                itemClear.isVisible = false
            }
        }
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.mnu_delete -> showCancellationConfirmationDialog()
            R.id.mnu_allot -> navigateToMachineSelectionScreen()
            R.id.mnu_invoice -> showInvoiceDetailsInputDialog()
            R.id.mnu_back -> showRevertConfirmationDialog()
            R.id.mnu_done -> showCompleteConfirmationDialog()
            R.id.mnu_clear_pending -> viewModel.clearPendingStatusOfSelectedItems(getDestinationId())
            R.id.mnu_mark_as_pending -> showPendingRemarksDialog()
        }

        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
        if (clearSelections)
            viewModel.jobSelections.clear()
        else
            clearSelections = true
    }

    private fun renderTitle(destination: Destination) {
        val duration = Duration.fromMinutes(destination.runningTime).toString()
        updateTitle(destination.name)
        updateSubTitle("$duration in ${destination.jobCount} jobs")

        if (getDestinationType() == Destination.TYPE_FIXED) {
            viewBinding.lblLastCompletionTime.visibility = View.GONE
        } else {
            viewBinding.lblLastCompletionTime.visibility = View.VISIBLE

            val dateString = SimpleDateFormat(
                getString(R.string.simple_format_last_completion),
                Locale.getDefault()
            )
                .format(Date(destination.lastJobCompletion))

            viewBinding.lblLastCompletionTime.text =
                getString(R.string.last_completed_on, dateString)
        }
    }


    override fun onDialogResult(dialogResult: Any, code: Int) {
        if (code == DIALOG_CODE_INVOICE_DETAIL) {
            viewModel.invoiceSelectedJob(getDestinationId(), dialogResult as String)
        }

        if (code == DIALOG_CODE_PENDING_REMARKS) {
            viewModel.markAsPending(getDestinationId(), dialogResult as String)
        }

    }

    private fun handleWorkingStatusEvent(loadingStatus: LoadingStatus) {


        when (loadingStatus) {

            is LoadingStatus.Loading -> {
                showWaitDialog(getString(R.string.one_moment_please))
            }

            is LoadingStatus.Error -> {
                hideWaitDialog()
                toast(loadingStatus.exception.message ?: getString(R.string.error_unknown_error))
            }

            is LoadingStatus.Success<*> -> {
                hideWaitDialog()

                //Finish the action mode. But don't refresh the adapter as the DiffUtil will take
                //care of it automatically
                //refreshAdapterNeeded = !(loadingStatus.data as Boolean)
                refreshAdapterNeeded = false
                actionMode?.finish()
            }


        }

    }

    private fun showInvoiceDetailsInputDialog() {
        DialogTextInput.getInstance(
            title = getString(R.string.invoice_detail),
            code = DIALOG_CODE_INVOICE_DETAIL
        )
            .show(
                childFragmentManager,
                DialogTextInput.TAG
            )
    }

    private fun showPendingRemarksDialog() {
        DialogTextInput.getInstance(
            getString(R.string.pending_remarks),
            "",
            DIALOG_CODE_PENDING_REMARKS,
            false
        ).show(
            childFragmentManager,
            DialogTextInput.TAG
        )

    }

    private fun showCancellationConfirmationDialog() {
        ConfirmationDialog.getInstance(
            getString(R.string.cancel_confirmation),
            getString(R.string.cancel),
            CONFIRMATION_CANCEL_JOBS
        ).show(
            childFragmentManager,
            ConfirmationDialog.TAG
        )
    }

    private fun showCompleteConfirmationDialog() {
        ConfirmationDialog.getInstance(
            getString(R.string.complete_confirmation),
            getString(R.string.mark_as_complete),
            CONFIRMATION_COMPLETE_JOBS
        ).show(
            childFragmentManager,
            ConfirmationDialog.TAG
        )
    }

    private fun showRevertConfirmationDialog() {
        ConfirmationDialog.getInstance(
            getString(R.string.revert_confirmation),
            getString(R.string.remove),
            CONFIRMATION_REVERT_JOBS
        ).show(
            childFragmentManager,
            ConfirmationDialog.TAG
        )
    }

    override fun onConfirmationDialogConfirm(confirmationId: Int, extra: String) {
        when (confirmationId) {
            CONFIRMATION_CANCEL_JOBS -> viewModel.cancelSelectedJobs(getDestinationId())
            CONFIRMATION_REVERT_JOBS -> viewModel.backtrackSelectedJobs(getDestinationId())
            CONFIRMATION_COMPLETE_JOBS -> viewModel.markSelectedJobsAsComplete(
                getDestinationId(),
                currentTimeInMillis()
            )
        }
    }

    private fun navigateToMachineSelectionScreen() {

        findNavController().navigate(
            R.id.action_fixedDestinationFragment_to_manageMachinesFragment,
            ManageMachinesFragment.getArguments(true)
        )
    }


    private fun navigateToViewPrintOrderScreen(printOrderId: String) {
        findNavController().navigate(
            R.id.action_fixedDestinationFragment_to_viewPrintOrderFragment,
            ViewPrintOrderFragment.getArguments(
                getDestinationId(),
                getDestinationType(),
                printOrderId
            )
        )
    }

    private fun getDestinationId(): String =
        arguments?.getString(KEY_DESTINATION_ID) ?: DatabaseContract.DOCUMENT_DEST_NEW_JOBS

    private fun getDestinationType(): Int =
        arguments?.getInt(KEY_DESTINATION_TYPE) ?: Destination.TYPE_FIXED
}