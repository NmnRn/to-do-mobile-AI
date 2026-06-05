package com.odak.app

import android.app.Application
import com.odak.app.data.AppDatabase
import com.odak.app.data.TaskRepository
import com.odak.app.util.Alert

class OdakApp : Application() {
    val repository: TaskRepository by lazy { TaskRepository(AppDatabase.get(this).taskDao()) }

    override fun onCreate() {
        super.onCreate()
        Alert.ensureChannel(this)
    }
}
