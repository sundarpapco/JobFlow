package com.sivakasi.papco.jobflow.screens.machines

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.nav3.util.ToasterState
import com.sivakasi.papco.jobflow.ui.TextInputDialogState
import com.sivakasi.papco.jobflow.util.LoadingStatus

@ExperimentalMaterialApi
class MachinesScreenUIState(
    private val context: Context
): ToasterState() {

    sealed interface Dialog {
        object None: Dialog
        object WaitDialog : Dialog
        data class EditMachineDialog(val state: TextInputDialogState<Destination>) : Dialog
        data class AddMachineDialog(val state: TextInputDialogState<Unit>) : Dialog
        data class DeleteConfirmationDialog(val deletingDestination: Destination) : Dialog
    }

    var dialog by mutableStateOf<Dialog>(Dialog.None)
        private set

    var machines: LoadingStatus by mutableStateOf(
        LoadingStatus.Loading(
            context.getString(R.string.one_moment_please)
        )
    )

    /*fun shouldShowFloatingActionButton(role:String): Boolean {
        if (selectionMode)
            return false

        return role == "root" || role == "admin"
    }*/

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

        dialog = Dialog.EditMachineDialog(state)
    }

    fun clearDialog(){
        dialog = Dialog.None
    }

    fun showAddMachineDialog() {

        val state = TextInputDialogState<Unit>(
            positiveButtonText = context.getString(R.string.save),
            negativeButtonText = context.getString(R.string.cancel)
        ).apply {
            title = context.getString(R.string.add_machine)
            label = context.getString(R.string.machine_name)
        }

        dialog = Dialog.AddMachineDialog(state)
    }

    fun showDeleteConfirmationDialog(destination: Destination) {
       dialog = Dialog.DeleteConfirmationDialog(destination)
    }

   /* fun shouldShowContextMenu(): Boolean =
        !selectionMode && (role == "admin" || role == "root")*/

    fun showWaitDialog() {
        dialog = Dialog.WaitDialog
    }

    fun getString(id: Int): String =
        context.getString(id)

}