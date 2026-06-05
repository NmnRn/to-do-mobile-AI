package com.odak.app.ui.timer

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {

    var elapsed by mutableStateOf(0L)
        private set
    var running by mutableStateOf(false)
        private set

    private val _laps = mutableStateListOf<Long>()
    val laps: List<Long> get() = _laps

    private var job: Job? = null
    private var base = 0L

    fun toggle() = if (running) pause() else start()

    fun start() {
        if (running) return
        running = true
        base = SystemClock.elapsedRealtime() - elapsed
        job = viewModelScope.launch {
            while (isActive) {
                elapsed = SystemClock.elapsedRealtime() - base
                delay(31)
            }
        }
    }

    fun pause() {
        running = false
        job?.cancel()
    }

    fun reset() {
        pause()
        elapsed = 0
        _laps.clear()
    }

    fun lap() {
        if (elapsed > 0) _laps.add(0, elapsed)
    }

    override fun onCleared() {
        job?.cancel()
    }
}
