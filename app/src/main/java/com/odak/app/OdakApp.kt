package com.odak.app

import android.app.Application
import android.content.Context
import com.odak.app.data.AppDatabase
import com.odak.app.data.PlanRepository
import com.odak.app.data.TaskRepository
import com.odak.app.util.LocaleManager
import com.odak.app.reminder.Reminders
import com.odak.app.task.TaskAlarms
import com.odak.app.util.Alert
import com.odak.app.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OdakApp : Application() {
    val repository: TaskRepository by lazy { TaskRepository(AppDatabase.get(this).taskDao()) }
    val planRepository: PlanRepository by lazy { PlanRepository(AppDatabase.get(this).planDao()) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        Alert.ensureChannel(this)
        Reminders.syncSchedule(this)
        appScope.launch {
            TaskAlarms.rescheduleAll(this@OdakApp, repository.timedTasks())
            TodayWidget.refresh(this@OdakApp)
        }
    }
}
