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

    @Query("SELECT * FROM tasks WHERE dueDate = :day ORDER BY status ASC, createdAt DESC")
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
}
