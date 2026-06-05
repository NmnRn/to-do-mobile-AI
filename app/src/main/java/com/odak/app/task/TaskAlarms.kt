package com.odak.app.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus

/**
 * Schedules exact, per-task clock alarms via [AlarmManager]. Each task uses its
 * id as the request code so re-scheduling replaces the previous alarm.
 */
object TaskAlarms {

    const val ACTION_FIRE = "com.odak.app.TASK_ALARM"
    const val EXTRA_TASK_ID = "task_id"

    private fun manager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Schedules (or cancels) the alarm for [task] based on its time/status. */
    fun sync(context: Context, task: Task) {
        val at = task.triggerAt
        if (at == null || task.status == TaskStatus.DONE || at <= System.currentTimeMillis()) {
            cancel(context, task.id)
            return
        }
        val am = manager(context)
        val pi = pendingIntent(context, task.id)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        runCatching {
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }
    }

    /** Reschedules at an explicit time (used by the snooze action). */
    fun scheduleAt(context: Context, taskId: Long, at: Long) {
        val am = manager(context)
        val pi = pendingIntent(context, taskId)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        runCatching {
            if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    fun cancel(context: Context, taskId: Long) {
        runCatching { manager(context).cancel(pendingIntent(context, taskId)) }
    }

    /** Re-registers alarms for every pending timed task (after boot / update). */
    suspend fun rescheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { sync(context, it) }
    }
}
