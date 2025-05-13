package com.sivakasi.papco.jobflow.screens.destination

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.JobListSelection
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.ui.TextInputDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class DestinationScreenDialog {
    data object None : DestinationScreenDialog()
    data object WaitDialog : DestinationScreenDialog()
    data object DeleteConfirmation : DestinationScreenDialog()
    data object RevertConfirmation : DestinationScreenDialog()
    data object MarkAsDoneConfirmation : DestinationScreenDialog()
    data object InvoiceDetail : DestinationScreenDialog()
    class MarkAsPending(val dialogState: TextInputDialogState<*>) : DestinationScreenDialog()
}

class DestinationScreenState(
    private val context: Context
) {

    val selection = JobListSelection(context)

    var jobs: List<PrintOrderUIModel>? by mutableStateOf(null)
        private set

    var destination: Destination by mutableStateOf(Destination())
    var dialog: DestinationScreenDialog by mutableStateOf(DestinationScreenDialog.None)

    //Positions to keep track of the positions of drag to update the jobs in repository
    private var dragFrom = -1
    private var dragTo = -1

    fun loadJobs(jobsList: List<PrintOrderUIModel>) {
        //Make sure that all the jobs in the selection are still in the list
        //Because, some other person might have moved the print order or done something similar
        //while we are making the selection
        if (selection.selectionCount > 0)
            selection.assertList(jobsList)
        jobs = jobsList
    }

    fun startWorking() {
        dialog = DestinationScreenDialog.WaitDialog
    }

    fun workCompleted() {
        selection.clear()
        hideDialog()
    }

    suspend fun workError(e: Exception) = withContext(Dispatchers.Main) {
        hideDialog()
        context.toastError(e)
    }

    fun hideDialog() {
        dialog = DestinationScreenDialog.None
    }

    fun showInvoiceDialog() {
        dialog = DestinationScreenDialog.InvoiceDetail
    }

    fun showMarkAsPendingDialog(reason: String = "") {
        val state = TextInputDialogState<Unit>(
            positiveButtonText = context.getString(R.string.save)
        ).apply {
            title = context.getString(R.string.pending_remarks)
            label = context.getString(R.string.remarks)
            if (reason.isNotBlank())
                text = TextFieldValue(reason)
        }

        dialog = DestinationScreenDialog.MarkAsPending(state)
    }

    fun showDeleteConfirmationDialog() {
        dialog = DestinationScreenDialog.DeleteConfirmation
    }

    fun showRevertConfirmationDialog() {
        dialog = DestinationScreenDialog.RevertConfirmation
    }

    fun showMarkAsDoneConfirmationDialog() {
        dialog = DestinationScreenDialog.MarkAsDoneConfirmation
    }

    fun dragJob(from: Int, to: Int) {

        val list = jobs?.toMutableList() ?: return

        if (dragFrom == -1)
            dragFrom = from
        dragTo = to

        val toPositionJob = list[to]
        val fromPositionJob = list[from]
        val fromListPosition = fromPositionJob.listPosition
        fromPositionJob.listPosition = toPositionJob.listPosition
        toPositionJob.listPosition = fromListPosition
        list[from] = toPositionJob
        list[to] = fromPositionJob
        jobs = list
    }

    //Returns the list of Jobs that needs to be updated in the repository
    fun onDragCompleted(): List<PrintOrderUIModel>? {

        if (dragFrom == -1 || dragTo == -1 || dragFrom == dragTo) {
            dragFrom = -1
            dragTo = -1
            return null
        }

        val updatingData = if (dragFrom > dragTo)
            jobs?.subList(dragTo, dragFrom + 1)
        else
            jobs?.subList(dragFrom, dragTo + 1)

        dragFrom = -1
        dragTo = -1

        return updatingData
    }
}