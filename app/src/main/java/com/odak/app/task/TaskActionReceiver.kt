package com.odak.app.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.odak.app.OdakApp
import com.odak.app.data.TaskStatus
import com.odak.app.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles the "Tamamlandı" / "Ertele" buttons shown on a task's alarm
 * notification.
 */
class TaskActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        if (taskId <= 0) return
        val action = intent.action ?: return
        val app = context.applicationContext as? OdakApp ?: return

        if (notifId >= 0) NotificationManagerCompat.from(context).cancel(notifId)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.getById(taskId) ?: return@launch
                when (action) {
                    ACTION_DONE -> {
                        TaskAlarms.cancel(context, taskId)
                        app.repository.upsert(task.copy(status = TaskStatus.DONE))
                        Recurrence.next(task)?.let { next ->
                            val newId = app.repository.upsert(next)
                            TaskAlarms.sync(context, next.copy(id = newId))
                        }
                    }
                    ACTION_SNOOZE -> {
                        val at = System.currentTimeMillis() + SNOOZE_MILLIS
                        TaskAlarms.scheduleAt(context, taskId, at)
                    }
                }
                TodayWidget.refresh(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.odak.app.action.TASK_DONE"
        const val ACTION_SNOOZE = "com.odak.app.action.TASK_SNOOZE"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_NOTIF_ID = "notif_id"
        const val SNOOZE_MILLIS = 60 * 60 * 1000L // 1 hour
    }
}
