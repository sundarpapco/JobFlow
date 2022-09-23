package com.sivakasi.papco.jobflow.extensions

import java.util.*

fun getCalendarInstance(): Calendar =
    Calendar.getInstance(Locale.getDefault())


fun currentTimeInMillis(): Long =
    getCalendarInstance().timeInMillis

fun Calendar.asDateString(): String {

    return String.format(
        "%02d/%02d/%04d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH) + 1,
        get(Calendar.YEAR)
    )
    //return "${get(Calendar.DAY_OF_MONTH)}/${get(Calendar.MONTH) + 1}/${get(Calendar.YEAR)}"
}

fun Calendar.asReadableTimeStamp(): String {

    val morningOrAfternoon = if (get(Calendar.AM_PM) == Calendar.AM)
        "AM"
    else
        "PM"

    //Even though the documentation says get(Calendar.Hour) will return hour in 12 hour format,
    //it returns 12 alone as 0 instead of 12. So, the following check
    var hour = get(Calendar.HOUR)
    if (hour == 0)
        hour = 12

    return String.format(
        "%02d/%02d/%04d, %02d:%02d",
        get(Calendar.DAY_OF_MONTH),
        get(Calendar.MONTH) + 1,
        get(Calendar.YEAR),
        hour,
        get(Calendar.MINUTE)
    ) + " " + morningOrAfternoon
    //return "${get(Calendar.DAY_OF_MONTH)}/${get(Calendar.MONTH) + 1}/${get(Calendar.YEAR)}"
}

fun calendarWithTime(time: Long): Calendar =
    getCalendarInstance().also { it.timeInMillis = time }