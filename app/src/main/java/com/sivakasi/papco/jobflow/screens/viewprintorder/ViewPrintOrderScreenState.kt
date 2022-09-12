package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.ui.MenuAction

@ExperimentalMaterialApi
class ViewPrintOrderScreenState {

    var printOrder: PrintOrder? by mutableStateOf(null)
    var destinationId: String? = null

    var isLoading: Boolean by mutableStateOf(true)
    var error: String? by mutableStateOf(null)
    var isWaiting: Boolean by mutableStateOf(false)
    var printOrderRenderInfo: PrintOrderRenderInfo? by mutableStateOf(null)
    var destinationName: String? by mutableStateOf(null)
    var toastError: String? by mutableStateOf(null)
    var fabShowing: Boolean by mutableStateOf(false)
    var menuItems: List<MenuAction>? by mutableStateOf(null)
    var modalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var poMoved: Boolean by mutableStateOf(false)
    var modalSheetContent: ModalSheetContent by mutableStateOf(ModalSheetContent.NONE)

    fun loadPrintOrder(
        context: Context,
        printOrder: PrintOrder,
        destinationId: String,
        userRole: String
    ) {


        this.printOrder = printOrder
        this.destinationId = destinationId
        printOrderRenderInfo = PrintOrderRenderInfo.from(context, printOrder)
        fabShowing = userRole != "printer"
        menuItems = prepareMenuItems(context, printOrder, destinationId, userRole)
        error = null
        isWaiting = false
        toastError = null
        isLoading = false

    }

    fun loadError(context: Context, e: Throwable) {
        error = e.message ?: context.getString(R.string.error_unknown_error)
        printOrder = null
        destinationId = null
        printOrderRenderInfo = null
        isLoading = false
        isWaiting = false
        menuItems = null
        toastError = null
        fabShowing = false
    }

    fun printOrderMoved() {
        isLoading = false
        poMoved = true
    }

    private fun prepareMenuItems(
        context: Context,
        printOrder: PrintOrder,
        destinationId: String,
        userRole: String
    ): List<MenuAction>? {

        //Printers cannot see the menu at all
        if (userRole == "printer")
            return null

        val menu = mutableListOf<MenuAction>()

        menu.add(MenuAction(R.drawable.ic_notes, null, context.getString(R.string.notes)))
        menu.add(MenuAction(R.drawable.ic_print, null, context.getString(R.string.print)))
        menu.add(MenuAction(null, null, context.getString(R.string.repeat_this_job)))
        menu.add(MenuAction(null, null, context.getString(R.string.menu_share_as_pdf)))
        menu.add(MenuAction(null, null, context.getString(R.string.processing_history)))

        val previousHistoryVisible = when {
            destinationId == DatabaseContract.DOCUMENT_DEST_COMPLETED -> false
            destinationId == DatabaseContract.DOCUMENT_DEST_CANCELLED -> false
            printOrder.jobType == PrintOrder.TYPE_NEW_JOB -> false
            printOrder.plateMakingDetail.plateNumber <= 0 -> false
            else -> true
        }

        if (previousHistoryVisible)
            menu.add(
                MenuAction(
                    null,
                    null,
                    context.getString(R.string.previous_processing_history)
                )
            )

        if (printOrder.partialDispatches.isNotEmpty()) {
            menu.add(
                MenuAction(
                    null,
                    null,
                    context.getString(R.string.partial_dispatches)
                )
            )
        }

        return menu

    }

    enum class ModalSheetContent {
        NONE, PART_DISPATCHES, PROCESSING_HISTORY
    }

}