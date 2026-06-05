package com.odak.app.data

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun statusToString(status: TaskStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun repeatToString(rule: RepeatRule): String = rule.name

    @TypeConverter
    fun stringToRepeat(value: String): RepeatRule =
        runCatching { RepeatRule.valueOf(value) }.getOrDefault(RepeatRule.NONE)

    @TypeConverter
    fun priorityToInt(priority: Priority): Int = priority.ordinal

    @TypeConverter
    fun intToPriority(value: Int): Priority =
        Priority.entries.getOrElse(value) { Priority.MEDIUM }

    @TypeConverter
    fun subtasksToString(list: List<SubTask>): String {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("t", it.title).put("d", it.done)) }
        return arr.toString()
    }

    @TypeConverter
    fun stringToSubtasks(value: String): List<SubTask> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(value)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                SubTask(o.getString("t"), o.optBoolean("d"))
            }
        }.getOrDefault(emptyList())
    }
}
