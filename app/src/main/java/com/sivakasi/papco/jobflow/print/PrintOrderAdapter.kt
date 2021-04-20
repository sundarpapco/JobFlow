package com.sivakasi.papco.jobflow.print

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.sivakasi.papco.jobflow.data.PrintOrder
import javax.inject.Inject

class PrintOrderAdapter @Inject constructor(
    private val printOrder:PrintOrder,
    private val printOrderReport: PrintOrderReport
):PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {

        if(cancellationSignal?.isCanceled == true){
            callback?.onLayoutCancelled()
            return
        }

        //Build the PrintDocumentInfo and return
        val info=PrintDocumentInfo.Builder("PrintOrder.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback?.onLayoutFinished(info,false)

    }

    override fun onWrite(
        pageRanges: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {

        try{
            printOrderReport.render(printOrder,destination!!)
            callback?.onWriteFinished(pageRanges)
        }catch (e:Exception){
            callback?.onWriteFailed(e.message)
        }

    }
}