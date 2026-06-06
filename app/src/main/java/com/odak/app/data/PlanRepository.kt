package com.odak.app.data

import kotlinx.coroutines.flow.Flow

class PlanRepository(private val dao: PlanDao) {

    fun blocksForDay(day: Long): Flow<List<PlanBlock>> = dao.observeForDay(day)

    suspend fun upsert(block: PlanBlock): Long =
        if (block.id == 0L) dao.insert(block) else { dao.update(block); block.id }

    suspend fun delete(block: PlanBlock) = dao.delete(block)

    suspend fun getById(id: Long): PlanBlock? = dao.getById(id)
}
