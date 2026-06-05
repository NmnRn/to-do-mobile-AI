package com.odak.app.task

import com.odak.app.data.RepeatRule
import com.odak.app.data.SubTask
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import com.odak.app.util.DateUtils
import java.util.Calendar

/**
 * Builds the next occurrence of a repeating [task], or null when it does not
 * repeat. The clone is a fresh WAITING task on the next matching day with its
 * checklist reset; the original stays as the completed record.
 */
object Recurrence {

    fun next(task: Task): Task? {
        if (task.repeat == RepeatRule.NONE) return null
        val nextDay = nextDay(task.dueDate, task.repeat) ?: return null
        return task.copy(
            id = 0,
            status = TaskStatus.WAITING,
            createdAt = System.currentTimeMillis(),
            focusSeconds = 0,
            dueDate = nextDay,
            subtasks = task.subtasks.map { SubTask(it.title, done = false) }
        )
    }

    private fun nextDay(from: Long, rule: RepeatRule): Long? = when (rule) {
        RepeatRule.NONE -> null
        RepeatRule.DAILY -> DateUtils.addDays(from, 1)
        RepeatRule.WEEKLY -> DateUtils.addDays(from, 7)
        RepeatRule.WEEKDAYS -> {
            var day = DateUtils.addDays(from, 1)
            while (isWeekend(day)) day = DateUtils.addDays(day, 1)
            day
        }
    }

    private fun isWeekend(dayStart: Long): Boolean {
        val dow = Calendar.getInstance().apply { timeInMillis = dayStart }
            .get(Calendar.DAY_OF_WEEK)
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }
}
