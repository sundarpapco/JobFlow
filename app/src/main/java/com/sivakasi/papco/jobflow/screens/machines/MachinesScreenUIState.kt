package com.sivakasi.papco.jobflow.screens.machines

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.ui.TextInputDialogState
import com.sivakasi.papco.jobflow.util.LoadingStatus

@ExperimentalMaterialApi
class MachinesScreenUIState(
    private val context: Context
) {

    var role: String = "printer"
    var selectionMode = false

    var editMachineDialogState: TextInputDialogState<Destination>? by mutableStateOf(null)
        private set

    var addMachineDialogState: TextInputDialogState<Unit>? by mutableStateOf(null)
        private set

    var deletingMachineId: String? by mutableStateOf(null)
        private set

    var machines: LoadingStatus by mutableStateOf(
        LoadingStatus.Loading(
            context.getString(R.string.one_moment_please)
        )
    )

    var isWaitDialogShowing: Boolean by mutableStateOf(false)
        private set

    val bottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)


    fun shouldShowFloatingActionButton(): Boolean {
        if (selectionMode)
            return false

        return role == "root" || role == "admin"
    }

    fun showEditMachineDialog(destination: Destination) {

        val state = TextInputDialogState<Destination>(
            positiveButtonText = context.getString(R.string.save),
            negativeButtonText = context.getString(R.string.cancel)
        ).apply {
            title = context.getString(R.string.edit_machine)
            label = context.getString(R.string.machine_name)
            text = TextFieldValue(
                destination.name,
                TextRange(destination.name.length)
            )
            data=destination
        }

        editMachineDialogState = state
    }

    fun hideEditMachineDialog() {
        editMachineDialogState = null
    }

    fun showAddMachineDialog() {

        val state = TextInputDialogState<Unit>(
            positiveButtonText = context.getString(R.string.save),
            negativeButtonText = context.getString(R.string.cancel)
        ).apply {
            title = context.getString(R.string.add_machine)
            label = context.getString(R.string.machine_name)
        }

        addMachineDialogState = state
    }

    fun hideAddMachineDialog() {
        addMachineDialogState = null
    }

    fun showDeleteConfirmationDialog(destinationId: String) {
        deletingMachineId = destinationId
    }

    fun hideDeleteConfirmationDialog() {
        deletingMachineId = null
    }

    fun shouldShowContextMenu(): Boolean =
        !selectionMode && (role == "admin" || role == "root")

    fun showWaitDialog() {
        isWaitDialogShowing = true
    }

    fun hideWaitDialog() {
        isWaitDialogShowing = false
    }

    fun getString(id: Int): String =
        context.getString(id)

}