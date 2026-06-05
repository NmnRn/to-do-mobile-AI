package com.odak.app.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules the recurring [ReminderWorker] that nudges the user about pending
 * tasks. The user controls the on/off switch, how often it fires and the nightly
 * quiet-hours window from Settings; choices are persisted in "odak_prefs".
 */
object Reminders {
    private const val WORK_NAME = "odak_reminders"
    private const val PREFS = "odak_prefs"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_INTERVAL = "reminders_interval_hours"
    private const val KEY_QUIET_START = "reminders_quiet_start"
    private const val KEY_QUIET_END = "reminders_quiet_end"

    /** Selectable reminder intervals, in hours. */
    val INTERVAL_OPTIONS = listOf(1, 2, 3, 6)

    private const val DEFAULT_INTERVAL = 3
    private const val DEFAULT_QUIET_START = 23
    private const val DEFAULT_QUIET_END = 8

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---- enabled ----
    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        syncSchedule(context)
    }

    // ---- interval ----
    fun intervalHours(context: Context): Int =
        prefs(context).getInt(KEY_INTERVAL, DEFAULT_INTERVAL)

    fun setIntervalHours(context: Context, hours: Int) {
        prefs(context).edit().putInt(KEY_INTERVAL, hours).apply()
        syncSchedule(context)
    }

    // ---- quiet hours ----
    fun quietStart(context: Context): Int =
        prefs(context).getInt(KEY_QUIET_START, DEFAULT_QUIET_START)

    fun quietEnd(context: Context): Int =
        prefs(context).getInt(KEY_QUIET_END, DEFAULT_QUIET_END)

    fun setQuietHours(context: Context, start: Int, end: Int) {
        prefs(context).edit()
            .putInt(KEY_QUIET_START, start.coerceIn(0, 23))
            .putInt(KEY_QUIET_END, end.coerceIn(0, 23))
            .apply()
    }

    /** True if the current hour falls inside the user's quiet-hours window. */
    fun inQuietHours(context: Context): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val start = quietStart(context)
        val end = quietEnd(context)
        if (start == end) return false
        // Window that wraps past midnight (e.g. 23 -> 8).
        return if (start < end) hour in start until end
        else hour >= start || hour < end
    }

    /** Brings the scheduled work in line with the saved preferences. */
    fun syncSchedule(context: Context) {
        if (isEnabled(context)) schedule(context) else cancel(context)
    }

    private fun schedule(context: Context) {
        val hours = intervalHours(context).toLong()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(hours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
