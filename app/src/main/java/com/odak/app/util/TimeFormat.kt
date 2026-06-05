package com.odak.app.util

import java.util.Locale

object TimeFormat {

    /** Stopwatch style: MM:SS.cs or H:MM:SS.cs */
    fun stopwatch(millis: Long): String {
        val totalSec = millis / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        val centis = (millis % 1000) / 10
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d.%02d", hours, minutes, seconds, centis)
        } else {
            String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
        }
    }

    /** Countdown style: MM:SS or H:MM:SS (rounds up to the next second). */
    fun clock(millis: Long): String {
        val totalSec = (millis + 999) / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
