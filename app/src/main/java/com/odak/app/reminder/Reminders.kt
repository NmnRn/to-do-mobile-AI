package com.odak.app.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the recurring [ReminderWorker] that nudges the user about pending
 * tasks. The user can switch it off from Settings; the choice is persisted in
 * the shared "odak_prefs" store.
 */
object Reminders {
    private const val WORK_NAME = "odak_reminders"
    private const val PREFS = "odak_prefs"
    private const val KEY_ENABLED = "reminders_enabled"

    /** How often the reminder fires. */
    private const val INTERVAL_HOURS = 3L

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    /** Brings the scheduled work in line with the saved preference. */
    fun syncSchedule(context: Context) {
        if (isEnabled(context)) schedule(context) else cancel(context)
    }

    private fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
