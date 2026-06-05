package com.odak.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

    /** Returns the given day's millis normalized to 00:00 local time. */
    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun today(): Long = startOfDay(System.currentTimeMillis())

    fun addDays(dayStart: Long, days: Int): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = dayStart }
        cal.add(Calendar.DAY_OF_YEAR, days)
        return startOfDay(cal.timeInMillis)
    }

    private val dayFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR"))

    fun label(dayStart: Long): String = when (dayStart) {
        today() -> "Bugün"
        addDays(today(), -1) -> "Dün"
        addDays(today(), 1) -> "Yarın"
        else -> dayFormat.format(dayStart)
    }

    fun fullDate(dayStart: Long): String = dayFormat.format(dayStart)
}
