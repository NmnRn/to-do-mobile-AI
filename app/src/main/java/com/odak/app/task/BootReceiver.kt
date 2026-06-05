package com.odak.app.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.odak.app.OdakApp
import com.odak.app.reminder.Reminders
import com.odak.app.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-arms task alarms and the periodic reminder after a reboot. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return
        val app = context.applicationContext as? OdakApp ?: return

        Reminders.syncSchedule(context)
        TodayWidget.refresh(context)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TaskAlarms.rescheduleAll(context, app.repository.timedTasks())
            } finally {
                pending.finish()
            }
        }
    }
}
