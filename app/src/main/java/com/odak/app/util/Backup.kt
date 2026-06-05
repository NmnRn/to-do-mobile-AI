package com.odak.app.util

import com.odak.app.data.Priority
import com.odak.app.data.RepeatRule
import com.odak.app.data.SubTask
import com.odak.app.data.Task
import com.odak.app.data.TaskStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializes the task list to / from a self-contained JSON document for backup
 * and restore. Photos are referenced by their stored path; image files are not
 * embedded.
 */
object Backup {

    private const val VERSION = 1

    fun toJson(tasks: List<Task>): String {
        val arr = JSONArray()
        tasks.forEach { t ->
            val subs = JSONArray()
            t.subtasks.forEach { subs.put(JSONObject().put("t", it.title).put("d", it.done)) }
            arr.put(
                JSONObject()
                    .put("title", t.title)
                    .put("note", t.note)
                    .put("status", t.status.name)
                    .put("photoPath", t.photoPath ?: JSONObject.NULL)
                    .put("dueDate", t.dueDate)
                    .put("dueMinute", t.dueMinute)
                    .put("priority", t.priority.name)
                    .put("category", t.category)
                    .put("repeat", t.repeat.name)
                    .put("subtasks", subs)
                    .put("createdAt", t.createdAt)
                    .put("focusSeconds", t.focusSeconds)
            )
        }
        return JSONObject()
            .put("version", VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("tasks", arr)
            .toString(2)
    }

    fun fromJson(text: String): List<Task> {
        val root = JSONObject(text)
        val arr = root.getJSONArray("tasks")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val subsArr = o.optJSONArray("subtasks") ?: JSONArray()
            val subs = (0 until subsArr.length()).map { j ->
                val s = subsArr.getJSONObject(j)
                SubTask(s.getString("t"), s.optBoolean("d"))
            }
            Task(
                title = o.getString("title"),
                note = o.optString("note", ""),
                status = enumOr(o.optString("status"), TaskStatus.WAITING),
                photoPath = if (o.isNull("photoPath")) null else o.optString("photoPath").ifBlank { null },
                dueDate = o.getLong("dueDate"),
                dueMinute = o.optInt("dueMinute", -1),
                priority = enumOr(o.optString("priority"), Priority.MEDIUM),
                category = o.optString("category", ""),
                repeat = enumOr(o.optString("repeat"), RepeatRule.NONE),
                subtasks = subs,
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                focusSeconds = o.optLong("focusSeconds", 0)
            )
        }
    }

    private inline fun <reified T : Enum<T>> enumOr(value: String?, default: T): T =
        runCatching { enumValueOf<T>(value ?: "") }.getOrDefault(default)
}
