package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.data.PaperDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class PaperDetailTest {

    @Test
    fun testTileNoUps(){
        val paper=PaperDetail(height = 58.5f,width = 91.0f)
        val steps=paper.tilePaperSize(60f,91f)
        assertEquals(0,steps)
    }

    @Test
    fun testTileOneUp(){
        val paper=PaperDetail(height = 58.5f,width = 91.0f)
        val steps=paper.tilePaperSize(58.5f,91f)
        assertEquals(1,steps)
    }

    @Test
    fun testTileTwoUps(){

        val paper=PaperDetail(height = 76f,width = 102f)
        val steps=paper.tilePaperSize(50.6f,75.4f)
        assertEquals(2,steps)

    }
}