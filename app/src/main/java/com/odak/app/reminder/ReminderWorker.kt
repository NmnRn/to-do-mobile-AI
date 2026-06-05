package com.odak.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.odak.app.OdakApp
import com.odak.app.util.Alert
import com.odak.app.util.DateUtils

/**
 * Periodically checks today's task list and, if anything is still pending,
 * posts a gentle reminder. Stays silent during the user's quiet hours.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? OdakApp ?: return Result.success()
        if (!Reminders.isEnabled(applicationContext)) return Result.success()
        if (Reminders.inQuietHours(applicationContext)) return Result.success()

        val pending = runCatching { app.repository.pendingForDay(DateUtils.today()) }
            .getOrElse { return Result.retry() }
        if (pending.isEmpty()) return Result.success()

        val count = pending.size
        val names = pending.take(3).joinToString("\n") { "• ${it.title}" }
        val title = "$count görev seni bekliyor"
        val message = buildString {
            append(names)
            if (count > 3) append("\n…ve ${count - 3} tane daha")
            append("\nBugün biraz ilerleyelim mi?")
        }
        Alert.fireReminder(applicationContext, title, message)
        return Result.success()
    }
}
