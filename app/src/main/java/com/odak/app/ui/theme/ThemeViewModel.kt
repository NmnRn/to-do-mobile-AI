package com.odak.app.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("odak_prefs", Context.MODE_PRIVATE)

    var mode by mutableStateOf(read())
        private set

    fun set(newMode: ThemeMode) {
        mode = newMode
        prefs.edit().putString(KEY, newMode.name).apply()
    }

    private fun read(): ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString(KEY, ThemeMode.SYSTEM.name)!!)
    }.getOrDefault(ThemeMode.SYSTEM)

    private companion object {
        const val KEY = "theme_mode"
    }
}
