package com.odak.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskStatus { WAITING, IN_PROGRESS, DONE }

/** How a task repeats once completed. */
enum class RepeatRule { NONE, DAILY, WEEKDAYS, WEEKLY }

/** Visual / sorting priority. Stored as ordinal so it sorts numerically. */
enum class Priority { LOW, MEDIUM, HIGH }

/** A single checklist item inside a task. */
data class SubTask(val title: String, val done: Boolean = false)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val status: TaskStatus = TaskStatus.WAITING,
    val photoPath: String? = null,
    /** Epoch millis normalized to the start of the day this task belongs to. */
    val dueDate: Long,
    /** Minutes from midnight for a timed reminder; -1 = no specific time. */
    val dueMinute: Int = -1,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "",
    val repeat: RepeatRule = RepeatRule.NONE,
    val subtasks: List<SubTask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    /** Total focus time (seconds) accumulated from the Pomodoro / timer. */
    val focusSeconds: Long = 0
) {
    /** Exact epoch-millis this task should alert at, or null when it has no time. */
    val triggerAt: Long?
        get() = if (dueMinute < 0) null else dueDate + dueMinute * 60_000L
}
