package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.extensions.asReadableTimeStamp
import com.sivakasi.papco.jobflow.extensions.calendarWithTime
import com.sivakasi.papco.jobflow.extensions.getCalendarInstance
import org.junit.Test

class CalendarTest {

    @Test
    fun testCalendarReadableTimeStamp(){

        val result = calendarWithTime(1661480397).asReadableTimeStamp()
        println(result)

    }
}