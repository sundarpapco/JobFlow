package com.sivakasi.papco.jobflow

import java.util.*

fun getCalendarInstance():Calendar=
    Calendar.getInstance(Locale.getDefault())

fun currentTimeInMillis():Long=
    getCalendarInstance().timeInMillis