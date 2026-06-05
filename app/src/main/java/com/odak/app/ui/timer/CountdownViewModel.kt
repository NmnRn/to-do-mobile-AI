package com.odak.app.ui.timer

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.timer.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CountdownViewModel(app: Application) : AndroidViewModel(app) {

    var totalMillis by mutableStateOf(5 * 60_000L)
        private set
    var remaining by mutableStateOf(5 * 60_000L)
        private set
    var running by mutableStateOf(false)
        private set

    private var job: Job? = null
    private var endAt = 0L

    val progress: Float
        get() = if (totalMillis <= 0) 0f else 1f - (remaining.toFloat() / totalMillis.toFloat())

    fun setMinutes(minutes: Int) {
        if (running) return
        val safe = minutes.coerceIn(1, 180)
        totalMillis = safe * 60_000L
        remaining = totalMillis
    }

    val totalSeconds: Long get() = totalMillis / 1000

    /** Manually adjust the duration by [delta] seconds (kept within 5s..180min). */
    fun changeSeconds(delta: Int) {
        if (running) return
        val newTotal = (totalSeconds + delta).coerceIn(5L, 180L * 60L)
        totalMillis = newTotal * 1000
        remaining = totalMillis
    }

    fun toggle(onFinish: () -> Unit) = if (running) pause() else start(onFinish)

    fun start(onFinish: () -> Unit) {
        if (running || remaining <= 0) return
        running = true
        endAt = SystemClock.elapsedRealtime() + remaining
        TimerService.startCountdown(
            getApplication<Application>(),
            TimerService.KEY_COUNTDOWN,
            endAt,
            title = "Zamanlayıcı",
            finishTitle = "Süre doldu",
            finishMessage = "Zamanlayıcı tamamlandı",
            finishAlertId = 1001
        )
        job = viewModelScope.launch {
            while (isActive) {
                remaining = (endAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)
                if (remaining <= 0) {
                    running = false
                    onFinish()
                    break
                }
                delay(50)
            }
        }
    }

    fun pause() {
        running = false
        job?.cancel()
        TimerService.stop(getApplication<Application>(), TimerService.KEY_COUNTDOWN)
    }

    fun reset() {
        pause()
        remaining = totalMillis
    }

    override fun onCleared() {
        job?.cancel()
    }
}
