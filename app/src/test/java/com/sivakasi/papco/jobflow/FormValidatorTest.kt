package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.util.FormValidator
import org.junit.Assert.assertEquals
import org.junit.Test

class FormValidatorTest {

    @Test
    fun testMixedTrueAndFalse(){
        val validator= FormValidator()

        validator
            .validate(true)
            .validate(false)
            .validate(true)
            .validate(true)
            .validate(true)

        assertEquals(false,validator.isValid())

    }

    @Test
    fun testAllTrue(){
        val validator= FormValidator()

        validator
            .validate(true)
            .validate(true)
            .validate(true)
            .validate(true)
            .validate(true)

        assertEquals(true,validator.isValid())

    }

    @Test
    fun testAllFalse(){
        val validator= FormValidator()

        validator
            .validate(false)
            .validate(false)
            .validate(false)
            .validate(false)
            .validate(false)

        assertEquals(false,validator.isValid())

    }

}