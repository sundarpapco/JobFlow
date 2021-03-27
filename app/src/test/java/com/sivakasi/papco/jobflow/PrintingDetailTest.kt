package com.sivakasi.papco.jobflow

import android.os.Bundle
import com.sivakasi.papco.jobflow.data.PrintingDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class PrintingDetailTest {

    @Test
    fun testWriteToParcel(){

        val outState= Bundle()
        val testObject=PrintingDetail()
        testObject.colours="6"
        testObject.printingInstructions="instruction"
        testObject.hasSpotColours=true
        testObject.runningMinutes=73

        testObject.writeToBundle(outState)
        val result= PrintingDetail.readFromBundle(outState) ?: error("Cannot read from bundle")

        assertEquals("6",result.colours)
        assertEquals("instruction",result.printingInstructions)
        assertEquals(true,result.hasSpotColours)
        assertEquals(73,result.runningMinutes)

    }

}