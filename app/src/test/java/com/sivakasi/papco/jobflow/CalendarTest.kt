package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.extensions.asCommaSeparatedNumber
import com.sivakasi.papco.jobflow.extensions.asReadableTimeStamp
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import org.junit.Test

class CalendarTest {

    @Test
    fun testCalendarReadableTimeStamp(){

        val result = calendarWithTime(1663746322000).asReadableTimeStamp()
        println(result)

    }

    @Test
    fun commaSeparatedNumberTest(){
        val number=123456
        println(number.asCommaSeparatedNumber())
    }
}