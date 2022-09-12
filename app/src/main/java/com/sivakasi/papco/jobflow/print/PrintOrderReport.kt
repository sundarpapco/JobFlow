package com.sivakasi.papco.jobflow.print

import android.app.Application
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.ParcelFileDescriptor
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.abs

class PrintOrderReport @Inject constructor(
    private val application: Application,
    private val fontArial: Typeface?
) {

    private val pageHeight = 842
    private val pageWidth = 595

    private val leftMargin = 27.58f
    private val cellMargin = 12f
    private val rowHeight = 25.92f
    private val rowWidth = 536.96f

    private lateinit var printOrder: PrintOrder

    private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val subHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val detailTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val lastRowOfPaperDetail: Int
        get() = 7 + printOrder.paperDetails!!.size + 1


    private lateinit var pdfDocument: PdfDocument

    fun print(
        printOrder: PrintOrder,
        destination: ParcelFileDescriptor
    ) {
        this.printOrder=printOrder
        drawPdf()
        pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
        pdfDocument.close()

    }

    suspend fun generatePdfFile(printOrder:PrintOrder):String= withContext(Dispatchers.IO){
        this@PrintOrderReport.printOrder=printOrder
        drawPdf()
        writeToPdfFile(pdfDocument)
    }

    private fun drawPdf(){
        val page = initialize()
        drawHeading(page.canvas)
        drawSubHeading(page.canvas)
        drawJobDetails(page.canvas)
        drawPaperDetails(page.canvas, printOrder)
        drawPlateMakingDetails(page.canvas, printOrder)
        drawPrintingDetail(page.canvas)
        drawPostPressDetails(page.canvas)
        drawFooter(page.canvas)

        pdfDocument.finishPage(page)
    }

    private fun initialize(): PdfDocument.Page {
        headingPaint.typeface = Typeface.create(fontArial, Typeface.BOLD)
        headingPaint.textSize = 18f

        subHeadingPaint.typeface = fontArial
        subHeadingPaint.textSize = 18f
        subHeadingPaint.flags = Paint.UNDERLINE_TEXT_FLAG

        labelTextPaint.typeface = Typeface.create(fontArial, Typeface.BOLD)
        labelTextPaint.textSize = 12f

        detailTextPaint.typeface = fontArial
        detailTextPaint.textSize = 12f

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 0.57f


        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight, 1
        ).create()

        pdfDocument = PdfDocument()
        return pdfDocument.startPage(pageInfo)
    }

    private fun drawHeading(canvas: Canvas) {

        val heading = "PAPCO OFFSET PRIVATE LIMITED"
        val textWidth = headingPaint.measureText(heading)
        val bounds=rowBounds(2)
        drawTextInBounds(canvas,heading,headingPaint,bounds,(rowWidth-textWidth)/2)

    }

    private fun drawSubHeading(canvas: Canvas) {
        val heading = "PRINT ORDER"
        val textWidth = subHeadingPaint.measureText(heading)
        val bounds=rowBounds(3)
        drawTextInBounds(canvas,heading,subHeadingPaint,bounds,(rowWidth-textWidth)/2)
    }

    private fun drawJobDetails(canvas: Canvas) {
        var rowNumber=4
        var bounds=rowBounds(rowNumber)
        bounds.left -= cellMargin
        drawLabeledText(canvas,"PO No",printOrder.printOrderNumber.toString(),bounds)

        var labelText="Date"
        var detailText= calendarWithTime(printOrder.creationTime).asDateString()
        var textWidth=labeledTextWidth(labelText,detailText)
        bounds.left += rowWidth-textWidth
        drawLabeledText(canvas,labelText,detailText,bounds)

        rowNumber++
        bounds=rowBounds(rowNumber)
        bounds.left -= cellMargin
        drawLabeledText(canvas,"Billing Name",printOrder.billingName,bounds)

        labelText="Plate number"
        detailText=if(printOrder.plateMakingDetail.plateNumber==PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
            "Outside plate"
        else {

            if(printOrder.jobType==PrintOrder.TYPE_REPEAT_JOB)
                "${printOrder.plateMakingDetail.plateNumber} (Old)"
            else
                printOrder.plateMakingDetail.plateNumber.toString()
        }
        textWidth=labeledTextWidth(labelText,detailText)
        bounds.left += rowWidth-textWidth
        drawLabeledText(canvas,labelText,detailText,bounds)

        rowNumber++
        bounds=rowBounds(rowNumber)
        bounds.left -= cellMargin
        drawLabeledText(canvas,"Job Name",printOrder.jobName,bounds)
    }

    private fun drawPaperDetails(canvas: Canvas, printOrder: PrintOrder) {

        drawSectionHeading(canvas, "Paper Details", 7)

        val rectangleBounds = rowRangeBounds(8, lastRowOfPaperDetail)
        canvas.drawRect(rectangleBounds, linePaint)

        for ((index, paperDetail) in printOrder.paperDetails!!.withIndex()) {
            drawPaperDetail(index, paperDetail, canvas, 8 + index)
        }

        val printingSizeDetail = printOrder.printingSizePaperDetail()
        drawPaperDetail(0, printingSizeDetail, canvas, 8 + printOrder.paperDetails!!.size, true)
    }


    private fun drawPaperDetail(
        index: Int,
        paperDetail: PaperDetail,
        canvas: Canvas,
        rowNumber: Int,
        isPrintingSize: Boolean = false
    ) {

        val bounds = rowBounds(rowNumber)
        val owner = when {
            paperDetail.partyPaper -> "Party's Own"
            else -> "Our Own"
        }
        val labelText = if (isPrintingSize) "Printing Size" else "${index + 1}. $owner"
        val detailText =
            if (isPrintingSize) paperDetail.asConsolidatedString() else paperDetail.toString()
        drawLabeledText(canvas, labelText, detailText, bounds)
    }

    private fun drawPlateMakingDetails(canvas: Canvas, printOrder: PrintOrder) {

        val plateMakingDetail = printOrder.plateMakingDetail
        var rowNumber = lastRowOfPaperDetail + 1
        drawSectionHeading(canvas, "Plate making Details", rowNumber)

        rowNumber++
        val rectangleBounds = rowRangeBounds(rowNumber, rowNumber + 3)
        canvas.drawRect(rectangleBounds, linePaint)

        drawPlateMakingDetailRow(
            canvas,
            rowNumber,
            "Trimming Size",
            plateMakingDetail.trimmingSize,
            "Machine",
            plateMakingDetail.machine
        )

        rowNumber++
        drawPlateMakingDetailRow(
            canvas,
            rowNumber,
            "Job Size",
            plateMakingDetail.jobSize,
            "Screen",
            plateMakingDetail.screen
        )

        rowNumber++
        drawPlateMakingDetailRow(
            canvas,
            rowNumber,
            "Gripper",
            plateMakingDetail.gripperSize,
            "Backside",
            plateMakingDetail.backsidePrinting
        )

        rowNumber++
        drawPlateMakingDetailRow(
            canvas,
            rowNumber,
            "Tail",
            plateMakingDetail.tailSize,
            "Backside Machine",
            plateMakingDetail.backsideMachine
        )

    }


    private fun drawPrintingDetail(canvas: Canvas) {

        var rowNumber = lastRowOfPaperDetail + 6
        drawSectionHeading(canvas, "Printing Details", rowNumber)

        rowNumber++
        canvas.drawRect(rowRangeBounds(rowNumber, rowNumber + 6), linePaint)
        var bounds = rowBounds(rowNumber)

        val plateDetail = if (printOrder.jobType == PrintOrder.TYPE_NEW_JOB)
            "NEW PLATE"
        else
            "REPRINT"

        drawTextInBounds(canvas, plateDetail, labelTextPaint, bounds, cellMargin)

        //Measure the color label text and detail text to right align it
        val colorLabelText = "Colours"
        val colorDetailText = printOrder.printingDetail.colours
        val textLength =
            labeledTextWidth(colorLabelText,colorDetailText)
        bounds.left = rowWidth - textLength
        drawLabeledText(canvas, colorLabelText, colorDetailText, bounds)

        //Draw the printing detail
        rowNumber++
        bounds = rowBounds(rowNumber)
        drawMultiLineText(
            canvas,
            bounds.left + cellMargin,
            bounds.top + cellMargin * 2,
            printOrder.printingDetail.printingInstructions,
            detailTextPaint
        )

    }

    private fun drawPostPressDetails(canvas: Canvas) {

        val numberOfPostPress = postPressCount()
        if (numberOfPostPress == 0)
            return

        var rowNumber = lastRowOfPaperDetail + 14
        drawSectionHeading(canvas, "Post Press Details", rowNumber)
        val rowsForPostPress: Int = if (numberOfPostPress % 3 == 0)
            numberOfPostPress / 3 * 2
        else
            (numberOfPostPress / 3 + 1) * 2

        //Draw the Box for postPress operation
        rowNumber++
        var column = 1
        canvas.drawRect(rowRangeBounds(rowNumber, rowNumber + (rowsForPostPress - 1)), linePaint)

        printOrder.lamination?.let {
            val detailText="${it}\n${it.remarks}"
            drawPostPressDetail(canvas,rowNumber,column,"Lamination",detailText)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.foil?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Foil",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.scoring?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Scoring",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.folding?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Folding",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.binding?.let {
            val detailText="${it.getBindingName(application)}\n${it.remarks}"
            drawPostPressDetail(canvas,rowNumber,column,"Binding",detailText)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.spotUV?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Spot UV",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.aqueousCoating?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Aqueous Coating",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.cutting?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Cutting",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

        printOrder.packing?.let {
            drawPostPressDetail(canvas,rowNumber,column,"Packing",it)
            if(column==3){
                column=1
                rowNumber+=2
            }else
                column++
        }

    }

    private fun drawFooter(canvas: Canvas){

        //Last row is 32
        val rowNumber=32
        val bounds=rowBounds(rowNumber)
        val footer="Approved By                    Checked By"
        val textWidth=detailTextPaint.measureText(footer)
        bounds.left += rowWidth-textWidth
        drawTextInBounds(canvas,footer,detailTextPaint,bounds,0f)

    }

    private fun drawPlateMakingDetailRow(
        canvas: Canvas,
        rowNumber: Int,
        label1: String,
        detail1: String,
        label2: String,
        detail2: String
    ) {

        val labelTextWidth = labelTextPaint.measureText("Backside Machine")
        val bounds = rowBounds(rowNumber)
        drawLabeledText(canvas, label1, detail1, bounds, labelTextWidth)
        bounds.left = leftMargin + rowWidth / 2
        drawLabeledText(canvas, label2, detail2, bounds, labelTextWidth)

    }

    private fun drawSectionHeading(canvas: Canvas, heading: String, rowNumber: Int) {

        val sectionHeight = 18f

        val sectionHeadingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        sectionHeadingTextPaint.color = Color.WHITE
        sectionHeadingTextPaint.textSize = 12f
        sectionHeadingTextPaint.typeface = Typeface.create(fontArial, Typeface.BOLD)

        val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        sectionHeadingPaint.style = Paint.Style.FILL
        sectionHeadingPaint.color = Color.BLACK

        val textWidth=sectionHeadingTextPaint.measureText(heading)+cellMargin*2
        val y = rowNumber * rowHeight - sectionHeight
        val bounds = RectF(leftMargin, y, leftMargin + textWidth, y + sectionHeight)
        canvas.drawRect(bounds, sectionHeadingPaint)


        drawTextInBounds(canvas, heading, sectionHeadingTextPaint, bounds, cellMargin)

    }

    private fun drawMultiLineText(
        canvas: Canvas,
        x: Float,
        y: Float,
        text: String,
        textPaint: Paint
    ) {

        val textHeight = paintHeight(textPaint)
        var calculatedY = y
        val multiLineText = text.split("\n")
        for (line in multiLineText) {
            canvas.drawText(line, x, calculatedY, textPaint)
            calculatedY += textHeight + 3
        }

    }

    private fun drawLabeledText(
        canvas: Canvas,
        label: String,
        detail: String,
        bounds: RectF,
        labelFieldWidth: Float = -1f
    ) {

        val labelTextWidth: Float =
            if (labelFieldWidth <= 0) labelTextPaint.measureText(label) else labelFieldWidth
        val useBounds = RectF(bounds)
        drawTextInBounds(canvas, label, labelTextPaint, useBounds, cellMargin)
        useBounds.left += labelTextWidth
        drawTextInBounds(canvas, ": $detail", detailTextPaint, useBounds, cellMargin)

    }

    private fun drawTextInBounds(
        canvas: Canvas,
        text: String,
        paint: Paint,
        bounds: RectF,
        margin: Float,
    ) {
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textHeight = abs(textBounds.top) + abs(textBounds.bottom)
        val yMargin = (bounds.height() - textHeight) / 2f
        val calculatedY = bounds.top + yMargin + abs(textBounds.top)

        canvas.drawText(
            text,
            bounds.left + margin,
            calculatedY,
            paint
        )
    }

    private fun paintHeight(textPaint: Paint): Int {
        val text="Py"
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        return abs(textBounds.top) + abs(textBounds.bottom)
    }

    private fun rowBounds(rowNumber: Int): RectF {
        return RectF(
            leftMargin,
             (rowNumber - 1) * rowHeight,
            leftMargin + rowWidth,
            (rowNumber - 1) * rowHeight + rowHeight
        )
    }

    private fun rowRangeBounds(fromRow: Int, toRow: Int): RectF {
        return RectF(
            leftMargin,
            (fromRow - 1) * rowHeight,
            leftMargin + rowWidth,
            toRow * rowHeight
        )
    }

    private fun drawPostPressDetail(
        canvas: Canvas,
        rowNumber: Int,
        columnNumber: Int,
        label: String,
        detail: String
    ) {

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        detailPaint.typeface = fontArial
        detailPaint.textSize = 8f
        val bounds = rowBounds(rowNumber)
        bounds.left += rowWidth / 3 * (columnNumber - 1)
        drawTextInBounds(canvas, label, labelTextPaint, bounds, cellMargin)
        if (detail.isNotBlank()) {
            drawMultiLineText(
                canvas,
                bounds.left + cellMargin,
                bounds.bottom + 3,
                detail, detailPaint
            )
        }

    }

    private fun postPressCount(): Int {
        var count = 0
        printOrder.lamination?.let { count++ }
        printOrder.foil?.let { count++ }
        printOrder.scoring?.let { count++ }
        printOrder.folding?.let { count++ }
        printOrder.binding?.let { count++ }
        printOrder.spotUV?.let { count }
        printOrder.aqueousCoating?.let { count++ }
        printOrder.cutting?.let { count++ }
        printOrder.packing?.let { count++ }
        return count
    }

    private fun labeledTextWidth(label:String,detail:String):Float=
        labelTextPaint.measureText(label)+detailTextPaint.measureText(": $detail")

    private fun writeToPdfFile(pdfDocument: PdfDocument): String {

        //create outputStream
        val cacheDirectoryPath=application.cacheDir.absolutePath
        val cacheDirectory = File(cacheDirectoryPath)
        if (!cacheDirectory.isDirectory)
            cacheDirectory.mkdirs()

        val filePath = "$cacheDirectory/${printOrder.documentId()}.pdf"

        val outStream = FileOutputStream(File(filePath))
        pdfDocument.writeTo(outStream)

        return filePath

    }
}
