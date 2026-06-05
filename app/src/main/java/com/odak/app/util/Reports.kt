package com.odak.app.util

import com.odak.app.data.TaskRepository
import com.odak.app.data.TaskStatus

/** A single day's slice in the weekly report. */
data class DayStat(val dayStart: Long, val done: Int, val total: Int)

/** Aggregated stats for the trailing 7 days. */
data class WeeklyReport(
    val days: List<DayStat>,
    val totalDone: Int,
    val totalTasks: Int,
    val focusMinutes: Long
) {
    val maxDone: Int get() = days.maxOfOrNull { it.done } ?: 0
}

object Reports {

    /** Builds a report covering today and the previous six days. */
    suspend fun weekly(repo: TaskRepository): WeeklyReport {
        val today = DateUtils.today()
        val start = DateUtils.addDays(today, -6)
        val tasks = repo.between(start, today)

        val days = (0..6).map { offset ->
            val day = DateUtils.addDays(start, offset)
            val ofDay = tasks.filter { it.dueDate == day }
            DayStat(
                dayStart = day,
                done = ofDay.count { it.status == TaskStatus.DONE },
                total = ofDay.size
            )
        }
        return WeeklyReport(
            days = days,
            totalDone = tasks.count { it.status == TaskStatus.DONE },
            totalTasks = tasks.size,
            focusMinutes = tasks.sumOf { it.focusSeconds } / 60
        )
    }
}
