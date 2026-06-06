package com.odak.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM plan_blocks WHERE dayDate = :day ORDER BY startMinute, endMinute")
    fun observeForDay(day: Long): Flow<List<PlanBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: PlanBlock): Long

    @Update
    suspend fun update(block: PlanBlock)

    @Delete
    suspend fun delete(block: PlanBlock)

    @Query("SELECT * FROM plan_blocks WHERE id = :id")
    suspend fun getById(id: Long): PlanBlock?
}
