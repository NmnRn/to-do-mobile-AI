package com.odak.app.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.odak.app.OdakApp
import com.odak.app.data.TaskStatus
import com.odak.app.util.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fired by [TaskAlarms] at a task's clock time; posts the alarm notification. */
class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskAlarms.EXTRA_TASK_ID, -1L)
        if (taskId <= 0) return
        val app = context.applicationContext as? OdakApp ?: return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.repository.getById(taskId) ?: return@launch
                if (task.status == TaskStatus.DONE) return@launch
                Alert.fireTaskAlarm(context, task)
            } finally {
                pending.finish()
            }
        }
    }
}
