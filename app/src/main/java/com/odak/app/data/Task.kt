package com.odak.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus { WAITING, IN_PROGRESS, DONE }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val status: TaskStatus = TaskStatus.WAITING,
    val photoPath: String? = null,
    /** Epoch millis normalized to the start of the day this task belongs to. */
    val dueDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    /** Total focus time (seconds) accumulated from the Pomodoro / timer. */
    val focusSeconds: Long = 0
)
