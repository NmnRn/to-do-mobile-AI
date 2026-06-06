package com.odak.app.util

import android.content.Context
import com.odak.app.R
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

    /** Short weekday label (e.g. "Pzt") for the weekly report. */
    fun shortWeekday(dayStart: Long): String =
        SimpleDateFormat("EEE", Locale.getDefault()).format(dayStart)

    fun label(context: Context, dayStart: Long): String = when (dayStart) {
        today() -> context.getString(R.string.day_today)
        addDays(today(), -1) -> context.getString(R.string.day_yesterday)
        addDays(today(), 1) -> context.getString(R.string.day_tomorrow)
        else -> fullDate(dayStart)
    }

    fun fullDate(dayStart: Long): String =
        SimpleDateFormat("d MMMM EEEE", Locale.getDefault()).format(dayStart)
}
