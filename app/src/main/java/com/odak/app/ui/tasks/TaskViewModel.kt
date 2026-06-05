package com.odak.app.ui.tasks

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.OdakApp
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import com.odak.app.util.DateUtils
import com.odak.app.util.ImageStorage
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
        viewModelScope.launch { repo.upsert(task.copy(status = next)) }
    }

    fun save(existing: Task?, title: String, note: String, status: TaskStatus, photoPath: String?) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            // If the photo was replaced or removed, clean up the old file.
            if (existing?.photoPath != null && existing.photoPath != photoPath) {
                ImageStorage.deleteQuietly(existing.photoPath)
            }
            val task = existing?.copy(
                title = cleanTitle,
                note = note.trim(),
                status = status,
                photoPath = photoPath
            ) ?: Task(
                title = cleanTitle,
                note = note.trim(),
                status = status,
                photoPath = photoPath,
                dueDate = selectedDay
            )
            repo.upsert(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            ImageStorage.deleteQuietly(task.photoPath)
            repo.delete(task)
        }
    }
}
