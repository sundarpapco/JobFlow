package com.sivakasi.papco.jobflow

import java.util.*

fun getCalendarInstance():Calendar=
    Calendar.getInstance(Locale.getDefault())


fun currentTimeInMillis():Long=
    getCalendarInstance().timeInMillis

fun Calendar.asDateString(): String {

    return String.format("%02d/%02d/%04d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH)+1,
        get(Calendar.YEAR))
    //return "${get(Calendar.DAY_OF_MONTH)}/${get(Calendar.MONTH) + 1}/${get(Calendar.YEAR)}"
}

fun calendarWithTime(time: Long): Calendar =
    getCalendarInstance().also { it.timeInMillis = time }