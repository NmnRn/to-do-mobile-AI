package com.odak.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single time block in the daily plan: "from start to end, do this". */
@Entity(tableName = "plan_blocks")
data class PlanBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch millis normalized to the start of the day this block belongs to. */
    val dayDate: Long,
    /** Minutes from midnight when the block starts. */
    val startMinute: Int,
    /** Minutes from midnight when the block ends. */
    val endMinute: Int,
    val title: String,
    val note: String = "",
    val done: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Length of the block in minutes (never negative). */
    val durationMinutes: Int get() = (endMinute - startMinute).coerceAtLeast(0)
}
