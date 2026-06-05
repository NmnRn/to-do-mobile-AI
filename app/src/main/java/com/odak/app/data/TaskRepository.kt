package com.odak.app.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun tasksForDay(day: Long): Flow<List<Task>> = dao.observeForDay(day)
    fun doneCount(day: Long): Flow<Int> = dao.observeDoneCount(day)
    fun totalCount(day: Long): Flow<Int> = dao.observeTotalCount(day)

    suspend fun upsert(task: Task): Long =
        if (task.id == 0L) dao.insert(task) else { dao.update(task); task.id }

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun addFocusSeconds(id: Long, seconds: Long) = dao.addFocusSeconds(id, seconds)

    suspend fun pendingForDay(day: Long): List<Task> = dao.pendingForDay(day)
}
