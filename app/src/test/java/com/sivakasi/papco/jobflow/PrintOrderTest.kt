package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.data.PlateMakingDetail
import com.sivakasi.papco.jobflow.data.PrintOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class PrintOrderTest {

    @Test
    fun testPrintSizeDetail(){

        val printOrder=PrintOrder()
        val paperDetail1=PaperDetail(
            true,
            58.5f,
            91f,
            130,
            "Real art paper",
            1000
        )

        val paperDetail2=PaperDetail(
            true,
            45.5f,
            58.5f,
            100,
            "Real art paper",
            1000
        )

        val plateMakingDetail=PlateMakingDetail()
        plateMakingDetail.trimmingHeight=455
        plateMakingDetail.trimmingWidth=585
        printOrder.plateMakingDetail=plateMakingDetail

        printOrder.paperDetails= mutableListOf(paperDetail1,paperDetail2)

        val resultPaper= printOrder.printingSizePaperDetail()

        assertEquals(45.5f,resultPaper.height)
        assertEquals(58.5f,resultPaper.width)
        assertEquals(0,resultPaper.gsm)
        assertEquals(3000,resultPaper.sheets)
        assertEquals("",resultPaper.name)
    }
}