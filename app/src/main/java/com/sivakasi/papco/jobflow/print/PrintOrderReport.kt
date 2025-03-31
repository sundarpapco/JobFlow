package com.sivakasi.papco.jobflow.print

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.ParcelFileDescriptor
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.data.printColors
import com.sivakasi.papco.jobflow.extensions.asDateString
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import androidx.core.graphics.createBitmap

class PrintOrderReport @Inject constructor(
    private val application: Application,
    private val fontArial: Typeface?
) {

    // All Dimensions are in Points
    // Page Size: 21 X 29.7 Cms (A4)
    private val pageHeight = 842
    private val pageWidth = 595

    private val leftMargin = 27.58f
    private val cellMargin = 12f
    private val rowHeight = 22.0f
    private val rowWidth = 536.96f

    private val sectionGap =15.0f // Gap between the sections
    private val sectionMargin=2f //Space after the last inside every section

    private lateinit var colors: ReportColors

    private lateinit var printOrder: PrintOrder


    //Initialize all the paints required
    private val textPaintNormal=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface=Typeface.create(fontArial, Typeface.NORMAL)
        color=Color.BLACK
    }

    private val textPaintBold=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface=Typeface.create(fontArial, Typeface.BOLD)
        color=Color.BLACK
    }

    private val linePaint by lazy{
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color=colors.primary
            style=Paint.Style.STROKE
            strokeWidth=1f
        }
    }

    private val sectionBoxPaint by lazy{
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color=colors.primaryContainer
            style=Paint.Style.FILL
        }
    }

    private val shadowPaint by lazy{
        Paint().apply {
            color = Color.WHITE // Color of the rectangle
            style = Paint.Style.FILL // Fill the rectangle
            setShadowLayer(3f, 3f, 4f, Color.GRAY) // Shadow properties
            isAntiAlias = true // Smooth edges
        }
    }

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

    suspend fun generatePdfFile(
        printOrder:PrintOrder
    ):String= withContext(Dispatchers.IO){
        this@PrintOrderReport.printOrder=printOrder
        drawPdf()
        writeToPdfFile(pdfDocument)
    }

    private fun drawPdf(){
        this.colors=printOrder.printColors()
        val page = initialize()
        drawLogo(page.canvas)
        //drawBitmapShadow(page.canvas,RectF(10f,10f,200f,100f))
        var yOffset = 4.5f*rowHeight
        drawCompanyName(page.canvas)
        drawPrintOrder(page.canvas)
        yOffset=drawJobDetails(page.canvas,yOffset)
        yOffset+=10f
        yOffset=drawPaperDetails(page.canvas, yOffset,printOrder)
        yOffset+=sectionGap
        yOffset=drawPlateMakingDetails(page.canvas, yOffset,printOrder)
        yOffset+=sectionGap
        yOffset=drawPrintingDetail(page.canvas,yOffset)
        yOffset+=sectionGap
        drawPostPressDetails(page.canvas,yOffset)
        drawFooter(page.canvas)

        pdfDocument.finishPage(page)
    }

    private fun initialize(): PdfDocument.Page {

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth,
            pageHeight, 1
        ).create()

        pdfDocument = PdfDocument()
        return pdfDocument.startPage(pageInfo)
    }

    private fun drawCompanyName(canvas: Canvas){

        val heading = "PAPCO OFFSET PRIVATE LIMITED"
        val fontGaramond = ResourcesCompat.getFont(application, R.font.garamond)
        val companyNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(fontGaramond, Typeface.NORMAL)
            textSize = 20f
            color = colors.primary
        }
        val textWidth = companyNamePaint.measureText(heading)
        val bounds=rowBounds(1.5f*rowHeight)
        drawTextInBounds(canvas,heading,companyNamePaint,bounds,(rowWidth-textWidth))

    }

    private fun drawPrintOrder(canvas: Canvas) {
        val printOrderText = "Print Order"
        val fontGaramond = ResourcesCompat.getFont(application, R.font.garamond)
        val printOrderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(fontGaramond, Typeface.NORMAL)
            textSize = 24f
            color = colors.onPrimaryContainer
        }
        val textWidth = printOrderPaint.measureText(printOrderText)
        val bounds=rowBounds(2.5f*rowHeight)
        drawTextInBounds(canvas,printOrderText,printOrderPaint,bounds,(rowWidth-textWidth))
    }

    private fun drawJobDetails(canvas: Canvas, startingYOffset:Float):Float {

        var currentYOffset=startingYOffset
        val labelDate = "Date: "
        val labelClient = "Client: "
        val labelJobName = "Job Name: "
        val labelPONumber = "PO No: "
        val labelPlateNumber = "Plate Number: "
        val detailDate = calendarWithTime(printOrder.creationTime).asDateString()
        val detailClient = printOrder.billingName
        val detailJobName = printOrder.jobName
        val detailPONumber = printOrder.printOrderNumber.toString()

        val detailPlateNumber = if(printOrder.plateMakingDetail.plateNumber==PlateMakingDetail.PLATE_NUMBER_OUTSIDE_PLATE)
            "Outside plate"
        else {

            if(printOrder.jobType==PrintOrder.TYPE_REPEAT_JOB)
                "${printOrder.plateMakingDetail.plateNumber} (Old)"
            else
                printOrder.plateMakingDetail.plateNumber.toString()
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(fontArial,Typeface.NORMAL)
            textSize = 12f
            color = colors.subtleText
        }

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(fontArial,Typeface.BOLD)
            textSize = 12f
            color = colors.onPrimaryContainer
        }

        var rowBounds = rowBounds(currentYOffset)
        val leftDetailStartingPosition= rowBounds.left + labelPaint.measureText(labelJobName)
        val rightDetailStartingPosition = run{
            val detailWidth= max(
                detailPaint.measureText(detailPlateNumber),
                detailPaint.measureText(detailPONumber)
            )
            rowBounds.left + rowWidth - detailWidth
        }

        val rightLabelStartingPosition = run{
            val plateNumberWidth = detailPaint.measureText(labelPlateNumber)
            rightDetailStartingPosition-plateNumberWidth
        }

        drawTextInBounds(canvas,labelDate,labelPaint,rowBounds,0f)
        rowBounds.left = leftDetailStartingPosition
        drawTextInBounds(canvas,detailDate,detailPaint,rowBounds,0f)
        rowBounds.left=rightLabelStartingPosition
        drawTextInBounds(canvas,labelPONumber,labelPaint,rowBounds,0f)
        rowBounds.left=rightDetailStartingPosition
        drawTextInBounds(canvas,detailPONumber,detailPaint,rowBounds,0f)
        currentYOffset+=rowHeight
        rowBounds= rowBounds(currentYOffset)
        drawTextInBounds(canvas,labelClient,labelPaint,rowBounds,0f)
        rowBounds.left = leftDetailStartingPosition
        drawTextInBounds(canvas,detailClient,detailPaint,rowBounds,0f)
        rowBounds.left=rightLabelStartingPosition
        drawTextInBounds(canvas,labelPlateNumber,labelPaint,rowBounds,0f)
        rowBounds.left=rightDetailStartingPosition
        drawTextInBounds(canvas,detailPlateNumber,detailPaint,rowBounds,0f)
        currentYOffset+=rowHeight
        rowBounds=rowBounds(currentYOffset)
        drawTextInBounds(canvas,labelJobName,labelPaint,rowBounds,0f)
        rowBounds.left = leftDetailStartingPosition
        drawTextInBounds(canvas,detailJobName,detailPaint,rowBounds,0f)
        return currentYOffset+rowHeight
    }


    private fun drawPaperDetails(canvas: Canvas, yOffset:Float, printOrder: PrintOrder):Float {

        var currentYOffset = yOffset

        val extraSpaceBetweenPaperDetailsAndPrintingSize=6f
        val sectionBounds = rowRangeBounds(currentYOffset, printOrder.paperDetails!!.size+2)
        sectionBounds.bottom += sectionMargin+extraSpaceBetweenPaperDetailsAndPrintingSize
        drawBitmapShadow(canvas,sectionBounds)
        canvas.drawRect(sectionBounds, sectionBoxPaint)
        drawSectionHeading(canvas, "Paper Details", currentYOffset,colors)

        for ((index, paperDetail) in printOrder.paperDetails!!.withIndex()) {
            currentYOffset+=rowHeight
            drawPaperDetail(index, paperDetail, canvas, currentYOffset)
        }

        currentYOffset+=rowHeight+extraSpaceBetweenPaperDetailsAndPrintingSize
        val printingSizeDetail = printOrder.printingSizePaperDetail()
        drawPaperDetail(0, printingSizeDetail, canvas, currentYOffset, true)
        canvas.drawLine(sectionBounds.left,sectionBounds.bottom,sectionBounds.right,sectionBounds.bottom,linePaint)
        return sectionBounds.bottom
    }


    private fun drawPaperDetail(
        index: Int,
        paperDetail: PaperDetail,
        canvas: Canvas,
        yOffset: Float,
        isPrintingSize: Boolean = false
    ) {

        val bounds = rowBounds(yOffset)
        val owner = when {
            paperDetail.partyPaper -> "Party's Own"
            else -> "Our Own"
        }
        val labelText = if (isPrintingSize) "Printing Size" else "${index + 1}. $owner"
        val detailText =
            if (isPrintingSize) paperDetail.asConsolidatedString() else paperDetail.toString()
    drawLabeledText(canvas, labelText, detailText, bounds)
    }


    private fun drawPlateMakingDetails(canvas: Canvas, yOffset:Float, printOrder: PrintOrder):Float {

        var currentYOffset=yOffset
        val plateMakingDetail = printOrder.plateMakingDetail

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(fontArial,Typeface.BOLD)
            color=colors.subtleText
        }

        val sectionBounds = rowRangeBounds(currentYOffset, 5)
        sectionBounds.bottom += sectionMargin
        drawBitmapShadow(canvas,sectionBounds)
        canvas.drawRect(sectionBounds, sectionBoxPaint)
        drawSectionHeading(canvas, "Plate making Details", currentYOffset,colors)

        currentYOffset+=rowHeight
        drawPlateMakingDetailRow(
            canvas,
            currentYOffset,
            "Trimming Size",
            plateMakingDetail.trimmingSize,
            "Machine",
            plateMakingDetail.machine,
            labelPaint
        )

        currentYOffset+=rowHeight
        drawPlateMakingDetailRow(
            canvas,
            currentYOffset,
            "Job Size",
            plateMakingDetail.jobSize,
            "Screen",
            plateMakingDetail.screen,
            labelPaint
        )

        currentYOffset+=rowHeight
        drawPlateMakingDetailRow(
            canvas,
            currentYOffset,
            "Gripper",
            plateMakingDetail.gripperSize,
            "Backside",
            plateMakingDetail.backsidePrinting,
            labelPaint
        )

        currentYOffset+=rowHeight
        drawPlateMakingDetailRow(
            canvas,
            currentYOffset,
            "Tail",
            plateMakingDetail.tailSize,
            "Backside Machine",
            plateMakingDetail.backsideMachine,
            labelPaint
        )

        canvas.drawLine(sectionBounds.left,sectionBounds.bottom,sectionBounds.right,sectionBounds.bottom,linePaint)
        return sectionBounds.bottom

    }


    private fun drawPrintingDetail(canvas: Canvas, yOffset:Float):Float {

        var currentYOffset=yOffset

        val sectionBounds = rowRangeBounds(currentYOffset, 9)
        sectionBounds.bottom += sectionMargin
        drawBitmapShadow(canvas,sectionBounds)
        canvas.drawRect(sectionBounds, sectionBoxPaint)
        drawSectionHeading(canvas, "Printing Details", currentYOffset,colors)

        currentYOffset+=rowHeight
        var bounds = rowBounds(currentYOffset)

        val plateDetail = if (printOrder.jobType == PrintOrder.TYPE_NEW_JOB)
            "NEW PLATE"
        else
            "REPRINT"

        drawTextInBounds(canvas, plateDetail, textPaintBold, bounds, cellMargin)

        //Measure the color label text and detail text to right align it
        val colorLabelText = "Colours"
        val colorDetailText = printOrder.printingDetail.colours
        val textLength =
            textPaintBold.measureText(colorLabelText)+textPaintNormal.measureText(": $colorDetailText")
        bounds.left = rowWidth - textLength
        drawLabeledText(canvas, colorLabelText, colorDetailText, bounds)

        //Draw the printing detail
        currentYOffset+=rowHeight
        bounds = rowBounds(currentYOffset)
        drawMultiLineText(
            canvas,
            bounds.left + cellMargin,
            bounds.top + cellMargin * 2,
            printOrder.printingDetail.printingInstructions,
            textPaintNormal
        )

        canvas.drawLine(sectionBounds.left,sectionBounds.bottom,sectionBounds.right,sectionBounds.bottom,linePaint)
        return sectionBounds.bottom

    }

    private fun drawPostPressDetails(canvas: Canvas, yOffset:Float):Float {

        var currentYOffset=yOffset
        val numberOfPostPress = postPressCount()
        if (numberOfPostPress == 0)
            return currentYOffset

        val rowsForPostPress: Int = if (numberOfPostPress % 3 == 0)
            numberOfPostPress / 3 * 2
        else
            (numberOfPostPress / 3 + 1) * 2

        val sectionBounds=rowRangeBounds(currentYOffset,rowsForPostPress+1)
        drawBitmapShadow(canvas,sectionBounds)
        canvas.drawRect(sectionBounds, sectionBoxPaint)
        drawSectionHeading(canvas, "Post Press Details", currentYOffset,colors)


        //Draw the Box for postPress operation
        currentYOffset+=rowHeight
        var column = 1
        printOrder.lamination?.let {
            val detailText="${it}\n${it.remarks}"
            drawPostPressDetail(canvas,currentYOffset,column,"Lamination",detailText)
            column++
        }

        printOrder.foil?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Foil",it)
            column++
        }

        printOrder.scoring?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Scoring",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.folding?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Folding",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.binding?.let {
            val detailText="${it.getBindingName(application)}\n${it.remarks}"
            drawPostPressDetail(canvas,currentYOffset,column,"Binding",detailText)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.spotUV?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Spot UV",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.aqueousCoating?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Aqueous Coating",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.cutting?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Cutting",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        printOrder.packing?.let {
            drawPostPressDetail(canvas,currentYOffset,column,"Packing",it)
            if(column==3){
                column=1
                currentYOffset+=(2*rowHeight)
            }else
                column++
        }

        canvas.drawLine(sectionBounds.left,sectionBounds.bottom,sectionBounds.right,sectionBounds.bottom,linePaint)
        return sectionBounds.bottom

    }

    private fun drawFooter(canvas: Canvas){

        //Last row is 32
        val yOffset=pageHeight-rowHeight*1.5f
        val bounds=rowBounds(yOffset)
        val footer="Approved By                    Checked By"
        val textWidth=textPaintNormal.measureText(footer)
        bounds.left += rowWidth-textWidth
        drawTextInBounds(canvas,footer,textPaintNormal,bounds,0f)

    }

    private fun drawPlateMakingDetailRow(
        canvas: Canvas,
        yOffset: Float,
        label1: String,
        detail1: String,
        label2: String,
        detail2: String,
        labelPaint: Paint
    ) {
        val labelTextWidth = labelPaint.measureText("Backside Machine")
        val bounds = rowBounds(yOffset)
        drawLabeledText(canvas, label1, detail1, bounds, labelTextWidth,labelPaint=labelPaint)
        bounds.left = leftMargin + rowWidth / 2
        drawLabeledText(canvas, label2, detail2, bounds, labelTextWidth,labelPaint=labelPaint)

    }

    private fun drawSectionHeading(canvas: Canvas, heading: String, yOffset: Float, colors: ReportColors) {

        val sectionHeight = 18f

        val sectionHeadingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color = Color.WHITE
            textSize=12f
            typeface=Typeface.create(fontArial, Typeface.BOLD)
        }

        val sectionHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style=Paint.Style.FILL
            color=colors.primary
        }

        val bounds=rowBounds(yOffset)
        bounds.bottom=bounds.top+sectionHeight
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
        labelFieldWidth: Float = -1f,
        labelPaint:Paint = textPaintBold,
        detailPaint: Paint = textPaintNormal
    ) {

        val labelTextWidth: Float =
            if (labelFieldWidth <= 0) labelPaint.measureText(label) else labelFieldWidth
        val useBounds = RectF(bounds)
        drawTextInBounds(canvas, label, labelPaint, useBounds, cellMargin)
        useBounds.left += labelTextWidth
        drawTextInBounds(canvas, ": $detail", detailPaint, useBounds, cellMargin)

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

    private fun rowBounds(yOffset: Float): RectF {
        return RectF(
            leftMargin,
             yOffset,
            leftMargin + rowWidth,
            yOffset+rowHeight
        )
    }

    private fun rowRangeBounds(yOffset: Float, rowCount: Int): RectF {
        return RectF(
            leftMargin,
            yOffset,
            leftMargin + rowWidth,
            yOffset+(rowCount*rowHeight)
        )
    }

    private fun drawPostPressDetail(
        canvas: Canvas,
        yOffset: Float,
        columnNumber: Int,
        label: String,
        detail: String
    ) {

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        detailPaint.typeface = fontArial
        detailPaint.textSize = 8f
        val bounds = rowBounds(yOffset)
        bounds.left += rowWidth / 3 * (columnNumber - 1)
        drawTextInBounds(canvas, label, textPaintBold, bounds, cellMargin)
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

    private fun drawBitmapShadow(pdfCanvas: Canvas,bounds: RectF) {

        // Set the dimensions for the bitmap
        val width = bounds.width() // Adjust as needed
        val height = bounds.height() // Adjust as needed

        // Create an empty bitmap with ARGB_8888 configuration
        val bitmap = createBitmap((width+10f).toInt(), (height+10f).toInt())

        // Create a canvas for the bitmap
        val canvas = Canvas(bitmap)

        // Enable hardware acceleration for shadow rendering
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw a rectangle with the shadow
        canvas.drawRect(0f, 0f, bounds.width(), bounds.height(), shadowPaint)
        pdfCanvas.drawBitmap(bitmap,bounds.left,bounds.top,null)

    }

    private fun drawLogo(canvas: Canvas){

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color=colors.primary
            style=Paint.Style.FILL
        }

        val yearPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface=Typeface.create(fontArial, Typeface.NORMAL)
            textSize=7f
            color=colors.primary
        }

        val companyPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface=Typeface.create(fontArial, Typeface.BOLD)
            textSize=9f
            color=colors.primary
        }

        val originX=leftMargin+cellMargin
        val originY=rowHeight*1.5f

        val path = Path()
        path.moveTo(originX,originY)
        path.lineTo(originX+26.3f,originY+62.3f)
        path.lineTo(originX+52.6f,originY)
        path.lineTo(originX+52.6f-18f,originY)
        path.lineTo(originX+52.6f-18f,originY+41.4f)
        path.lineTo(originX+18f,originY+41.4f)
        path.lineTo(originX+18f,originY)
        path.lineTo(originX,originY)
        canvas.drawPath(path,logoPaint)

        canvas.drawText("ESTD",originX,originY-2f,yearPaint)
        canvas.drawText("1954",originX+52.6f-18f,originY-2f,yearPaint)
        canvas.drawText("P",originX+23f,originY-1.6f,companyPaint)
        canvas.drawText("A",originX+23f,originY-1.6f+10f,companyPaint)
        canvas.drawText("P",originX+23f,originY-1.6f+20f,companyPaint)
        canvas.drawText("C",originX+23f,originY-1.6f+30f,companyPaint)
        canvas.drawText("O",originX+23f,originY-1.6f+40f,companyPaint)
    }
}
