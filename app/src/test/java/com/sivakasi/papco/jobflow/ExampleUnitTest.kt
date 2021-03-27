package com.sivakasi.papco.jobflow

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun floatToStringWithDecimal(){
        val result=58.5f.asString()
        assertEquals(result,"58.5")
    }

    @Test
    fun floatToStringWithoutDecimal(){
        val result=58.0f.asString()
        assertEquals(result,"58")
    }
}