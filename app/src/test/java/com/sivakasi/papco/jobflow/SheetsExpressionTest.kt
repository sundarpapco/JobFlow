package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.screens.manageprintorder.SheetsExpressionChecker
import org.junit.Assert.assertEquals
import org.junit.Test

class SheetsExpressionTest {

    @Test
    fun onlyReams(){
        val userInput="50r"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(25000,checker.totalSheets())
    }

    @Test
    fun reamsAndSheets(){
        val userInput="50r25"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(25025,checker.totalSheets())
    }

    @Test
    fun onlyGross(){
        val userInput="50g"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(7200,checker.totalSheets())
    }

    @Test
    fun grossWithSheets(){
        val userInput="50g14"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(7214,checker.totalSheets())
    }

    @Test
    fun reamSheetsWithMultiplier(){
        val userInput="5r14x3"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(7542,checker.totalSheets())
    }

    @Test
    fun reamSheetsWithMultiplierWithMultipleSets(){
        val userInput="5r14x3+10s"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(7552,checker.totalSheets())
    }

    @Test
    fun reamSheetsWithMultiplierWithMultipleSets_02(){
        val userInput="35gx2+5r25"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(12605,checker.totalSheets())
    }

    @Test
    fun reamSheetsWithMultiplierWithMultipleSets_03(){
        val userInput="10rx2+5r+3rx3"
        val checker=SheetsExpressionChecker(userInput)
        assertEquals(17000,checker.totalSheets())
    }

    @Test
    fun blankString_Invalid(){
        val userInput=""
        val checker = SheetsExpressionChecker(userInput)
        assertEquals(false,checker.isValid)
    }

    @Test
    fun invalidInput_01(){
        val userInput="20"
        val checker = SheetsExpressionChecker(userInput)
        assertEquals(false,checker.isValid)
    }

    @Test
    fun invalidInput_02(){
        val userInput="20r50s"
        val checker = SheetsExpressionChecker(userInput)
        assertEquals(false,checker.isValid)
    }

    @Test
    fun invalidInput_03(){
        val userInput="20r50r"
        val checker = SheetsExpressionChecker(userInput)
        assertEquals(false,checker.isValid)
    }

    @Test
    fun invalidInput_04(){
        val userInput="20rs"
        val checker = SheetsExpressionChecker(userInput)
        assertEquals(false,checker.isValid)
    }
}