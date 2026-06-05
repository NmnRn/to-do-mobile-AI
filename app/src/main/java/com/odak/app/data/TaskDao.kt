package com.odak.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks WHERE dueDate = :day " +
            "ORDER BY (status = 'DONE'), (dueMinute < 0), dueMinute, priority DESC, createdAt DESC"
    )
    fun observeForDay(day: Long): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate = :day AND status = 'DONE'")
    fun observeDoneCount(day: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate = :day")
    fun observeTotalCount(day: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE dueDate = :day AND status != 'DONE' ORDER BY createdAt DESC")
    suspend fun pendingForDay(day: Long): List<Task>

    @Query("UPDATE tasks SET focusSeconds = focusSeconds + :seconds WHERE id = :id")
    suspend fun addFocusSeconds(id: Long, seconds: Long)

    /** Tasks that carry a clock time and are not finished — used to (re)schedule alarms. */
    @Query("SELECT * FROM tasks WHERE dueMinute >= 0 AND status != 'DONE'")
    suspend fun timedTasks(): List<Task>

    /** Tasks whose day falls in [start, end] inclusive — used by the weekly report. */
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end")
    suspend fun between(start: Long, end: Long): List<Task>

    /** Distinct, non-empty category names for quick suggestions. */
    @Query("SELECT DISTINCT category FROM tasks WHERE category != '' ORDER BY category")
    suspend fun categories(): List<String>

    /** Every task, for backup/export. */
    @Query("SELECT * FROM tasks ORDER BY dueDate, createdAt")
    suspend fun all(): List<Task>

    @Query("DELETE FROM tasks")
    suspend fun clear()
}
