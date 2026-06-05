package com.odak.app.ui.tasks

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.OdakApp
import com.odak.app.data.Priority
import com.odak.app.data.RepeatRule
import com.odak.app.data.SubTask
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import com.odak.app.task.Recurrence
import com.odak.app.task.TaskAlarms
import com.odak.app.util.DateUtils
import com.odak.app.util.ImageStorage
import com.odak.app.widget.TodayWidget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as OdakApp).repository

    var selectedDay by mutableStateOf(DateUtils.today())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> = snapshotFlow { selectedDay }
        .flatMapLatest { repo.tasksForDay(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun changeDay(delta: Int) {
        selectedDay = DateUtils.addDays(selectedDay, delta)
    }

    fun goToday() {
        selectedDay = DateUtils.today()
    }

    fun cycleStatus(task: Task) {
        val next = when (task.status) {
            TaskStatus.WAITING -> TaskStatus.IN_PROGRESS
            TaskStatus.IN_PROGRESS -> TaskStatus.DONE
            TaskStatus.DONE -> TaskStatus.WAITING
        }
        viewModelScope.launch {
            val updated = task.copy(status = next)
            repo.upsert(updated)
            // Completing a repeating task spawns its next occurrence.
            if (next == TaskStatus.DONE && task.repeat != RepeatRule.NONE) {
                Recurrence.next(task)?.let { spawn ->
                    val newId = repo.upsert(spawn)
                    syncAlarm(spawn.copy(id = newId))
                }
            }
            syncAlarm(updated)
            refreshWidget()
        }
    }

    /** Toggles a single checklist item on a task. */
    fun toggleSubtask(task: Task, index: Int) {
        val subs = task.subtasks.toMutableList()
        if (index !in subs.indices) return
        subs[index] = subs[index].copy(done = !subs[index].done)
        viewModelScope.launch {
            repo.upsert(task.copy(subtasks = subs))
            refreshWidget()
        }
    }

    fun save(
        existing: Task?,
        title: String,
        note: String,
        status: TaskStatus,
        photoPath: String?,
        dueMinute: Int,
        priority: Priority,
        category: String,
        repeat: RepeatRule,
        subtasks: List<SubTask>
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            if (existing?.photoPath != null && existing.photoPath != photoPath) {
                ImageStorage.deleteQuietly(existing.photoPath)
            }
            val base = existing?.copy(
                title = cleanTitle,
                note = note.trim(),
                status = status,
                photoPath = photoPath,
                dueMinute = dueMinute,
                priority = priority,
                category = category.trim(),
                repeat = repeat,
                subtasks = subtasks
            ) ?: Task(
                title = cleanTitle,
                note = note.trim(),
                status = status,
                photoPath = photoPath,
                dueDate = selectedDay,
                dueMinute = dueMinute,
                priority = priority,
                category = category.trim(),
                repeat = repeat,
                subtasks = subtasks
            )
            val id = repo.upsert(base)
            syncAlarm(base.copy(id = id))
            refreshWidget()
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            ImageStorage.deleteQuietly(task.photoPath)
            TaskAlarms.cancel(getApplication<Application>(), task.id)
            repo.delete(task)
            refreshWidget()
        }
    }

    private fun syncAlarm(task: Task) {
        TaskAlarms.sync(getApplication<Application>(), task)
    }

    private fun refreshWidget() {
        TodayWidget.refresh(getApplication<Application>())
    }
}
