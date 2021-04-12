package com.sivakasi.papco.jobflow

import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.util.Duration
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun floatToStringWithDecimal(){

        val creationTime= getCalendarInstance().apply {
            set(Calendar.YEAR,2021)
            set(Calendar.MONTH,Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH,1)
            set(Calendar.HOUR_OF_DAY,8)
            set(Calendar.MINUTE,58)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }

        val checkingTime=getCalendarInstance().apply {
            set(Calendar.YEAR,2021)
            set(Calendar.MONTH,Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH,3)
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND,0)
            set(Calendar.MILLISECOND,0)
        }

        val printOrder=PrintOrder()
        printOrder.creationTime=creationTime.timeInMillis
        println(printOrder.ageString())
    }

    @Test
    fun durationTimeFormatTest(){
        val duration=Duration(6,28)
        println(duration.timeFormatString())
    }
}