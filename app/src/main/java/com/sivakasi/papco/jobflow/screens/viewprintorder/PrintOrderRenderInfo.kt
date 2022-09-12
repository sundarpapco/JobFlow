package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.content.Context
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.*
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime

/*
This class is designed as the rendering representation of a print order.
This class can be used to render the print order anywhere in the readable form like
rendering it in the screen or in the printout.

Consider creating this class in the background thread as an optimisation
 */

class PrintOrderRenderInfo {


    companion object {

        fun from(
            context: Context,
            printOrder: PrintOrder
        ): PrintOrderRenderInfo {

            return PrintOrderRenderInfo().apply {

                title = context.getString(R.string.po_xx, printOrder.printOrderNumber)
                poDetailsRenderInfo = PODetailsRenderInfo.from(context, printOrder)
                plateMakingDetailsRenderInfo =
                    PlateMakingDetailsRenderInfo.from(context, printOrder.plateMakingDetail)
                paperDetailsRenderInfo = printOrder.paperDetails?.map {
                    PaperDetailRenderInfo.from(context, it)
                } ?: emptyList()
                printingDetailRenderInfo = PrintingDetailsRenderInfo.from(printOrder.printingDetail)

                val postPress: MutableList<PostPressDetailRenderInfo> = mutableListOf()
                printOrder.lamination?.let {
                    postPress.add(
                        laminationRenderInfo(context, it)
                    )
                }

                printOrder.foil?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.foils),
                            remarks = it
                        )
                    )
                }

                printOrder.scoring?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.scoring),
                            remarks = it
                        )
                    )
                }

                printOrder.folding?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.folding),
                            remarks = it
                        )
                    )
                }

                printOrder.binding?.let {
                    postPress.add(
                        bindingRenderInfo(context, it)
                    )
                }

                printOrder.spotUV?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.spot_uv),
                            remarks = it
                        )
                    )
                }

                printOrder.aqueousCoating?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.aqueous_coating),
                            remarks = it
                        )
                    )
                }

                printOrder.cutting?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.cutting),
                            remarks = it
                        )
                    )
                }

                printOrder.packing?.let {
                    postPress.add(
                        PostPressDetailRenderInfo(
                            name = context.getString(R.string.packing),
                            remarks = it
                        )
                    )
                }

                postPressDetailRenderInfo = postPress
            }
        }


        private fun laminationRenderInfo(
            context: Context,
            lamination: Lamination
        ): PostPressDetailRenderInfo {
            return PostPressDetailRenderInfo().apply {
                name = context.getString(R.string.lamination)
                details = lamination.toString()
                remarks = lamination.remarks
            }
        }

        private fun bindingRenderInfo(
            context: Context,
            binding: Binding
        ): PostPressDetailRenderInfo {
            return PostPressDetailRenderInfo().apply {
                name = context.getString(R.string.binding)
                details = binding.getBindingName(context)
                remarks = binding.remarks
            }
        }

    }

    var title: String = ""
    var poDetailsRenderInfo: PODetailsRenderInfo = PODetailsRenderInfo()
    var plateMakingDetailsRenderInfo: PlateMakingDetailsRenderInfo = PlateMakingDetailsRenderInfo()
    var paperDetailsRenderInfo: List<PaperDetailRenderInfo> = emptyList()
    var printingDetailRenderInfo: PrintingDetailsRenderInfo = PrintingDetailsRenderInfo()
    var postPressDetailRenderInfo: List<PostPressDetailRenderInfo> = emptyList()

}

data class PODetailsRenderInfo(
    var date: String = "",
    var clientName: String = "",
    var jobName: String = "",
    var printingSize: String = "",
    var printingQuantity: String = "",
    var jobType: String = "",
    var invoice: String = ""
) {
    companion object {

        fun from(context: Context, printOrder: PrintOrder): PODetailsRenderInfo {

            return PODetailsRenderInfo().apply {

                date = calendarWithTime(printOrder.creationTime).asDateString()
                clientName = printOrder.billingName
                jobName = printOrder.jobName
                val paperDetail = printOrder.printingSizePaperDetail()
                printingSize = paperDetail.paperSize()
                printingQuantity = context.getString(R.string.xx_sheets, paperDetail.sheets)

                jobType = if (printOrder.jobType == PrintOrder.TYPE_NEW_JOB)
                    context.getString(R.string.new_job)
                else
                    context.getString(R.string.repeat_job)

                invoice = printOrder.invoiceDetails
            }
        }
    }
}

data class PlateMakingDetailsRenderInfo(
    var plateNumber: String = "",
    var trimmingSize: String = "",
    var jobSize: String = "",
    var gripper: String = "",
    var tail: String = "",
    var machine: String = "",
    var screen: String = "",
    var backside: String = "",
    var backsideMachine: String = ""
) {

    companion object {

        fun from(
            context: Context,
            plateMakingDetail: PlateMakingDetail
        ): PlateMakingDetailsRenderInfo {

            return PlateMakingDetailsRenderInfo().apply {

                plateNumber =
                    if (plateMakingDetail.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                        context.getString(R.string.outside_plate)
                    else
                        plateMakingDetail.plateNumber.toString()

                trimmingSize = context.getString(
                    R.string.paper_size,
                    plateMakingDetail.trimmingHeight,
                    plateMakingDetail.trimmingWidth
                )

                jobSize = if (plateMakingDetail.jobHeight <= 0)
                    "-"
                else
                    context.getString(
                        R.string.paper_size,
                        plateMakingDetail.jobHeight,
                        plateMakingDetail.jobWidth
                    )

                gripper = if (plateMakingDetail.gripper > 0)
                    context.getString(R.string.size_in_mm, plateMakingDetail.gripper)
                else
                    "-"

                tail = if (plateMakingDetail.tail > 0)
                    context.getString(R.string.size_in_mm, plateMakingDetail.tail)
                else
                    "-"

                machine = plateMakingDetail.machine

                screen =
                    if (plateMakingDetail.plateNumber == PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
                        "-"
                    else
                        plateMakingDetail.screen

                backside = plateMakingDetail.backsidePrinting
                backsideMachine = plateMakingDetail.backsideMachine

            }
        }

    }
}

data class PaperDetailRenderInfo(
    var owner: String = "",
    var sheets: String = "",
    var paperDetail: String = ""
) {

    companion object {

        fun from(context: Context, paperDetail: PaperDetail): PaperDetailRenderInfo {

            return PaperDetailRenderInfo().apply {
                owner = if (paperDetail.partyPaper)
                    context.getString(R.string.party_own)
                else
                    context.getString(R.string.our_own)

                sheets = context.getString(R.string.xx_sheets, paperDetail.sheets)
                this.paperDetail = paperDetail.paperName()
            }

        }

    }
}

data class PrintingDetailsRenderInfo(
    var colours: String = "",
    var printingInstructions: String = ""
) {
    companion object {
        fun from(printingDetail: PrintingDetail): PrintingDetailsRenderInfo {

            return PrintingDetailsRenderInfo().apply {
                colours = printingDetail.colours
                printingInstructions = printingDetail.printingInstructions
            }

        }
    }
}

data class PostPressDetailRenderInfo(
    var name: String = "",
    var details: String = "",
    var remarks: String = ""
)

