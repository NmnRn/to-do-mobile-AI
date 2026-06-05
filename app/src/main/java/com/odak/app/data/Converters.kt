package com.odak.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun statusToString(status: TaskStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}
