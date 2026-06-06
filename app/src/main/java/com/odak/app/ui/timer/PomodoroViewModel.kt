package com.odak.app.ui.timer

import android.app.Application
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odak.app.R
import com.odak.app.timer.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PomoPhase { WORK, SHORT_BREAK, LONG_BREAK }

@StringRes
fun PomoPhase.labelRes(): Int = when (this) {
    PomoPhase.WORK -> R.string.phase_work
    PomoPhase.SHORT_BREAK -> R.string.phase_short_break
    PomoPhase.LONG_BREAK -> R.string.phase_long_break
}

class PomodoroViewModel(app: Application) : AndroidViewModel(app) {

    var workMin by mutableStateOf(25)
        private set
    var shortMin by mutableStateOf(5)
        private set
    var longMin by mutableStateOf(15)
        private set
    private val cyclesBeforeLong = 4

    var phase by mutableStateOf(PomoPhase.WORK)
        private set
    var remaining by mutableStateOf(25 * 60_000L)
        private set
    var running by mutableStateOf(false)
        private set
    var completedSessions by mutableStateOf(0)
        private set

    private var job: Job? = null
    private var endAt = 0L

    fun phaseTotalMillis(): Long = when (phase) {
        PomoPhase.WORK -> workMin
        PomoPhase.SHORT_BREAK -> shortMin
        PomoPhase.LONG_BREAK -> longMin
    } * 60_000L

    val progress: Float
        get() {
            val total = phaseTotalMillis()
            return if (total <= 0) 0f else 1f - (remaining.toFloat() / total.toFloat())
        }

    fun setDurations(work: Int, short: Int, long: Int) {
        if (running) return
        workMin = work.coerceIn(1, 120)
        shortMin = short.coerceIn(1, 60)
        longMin = long.coerceIn(1, 60)
        remaining = phaseTotalMillis()
    }

    fun changeWork(delta: Int) = setDurations(workMin + delta, shortMin, longMin)
    fun changeShort(delta: Int) = setDurations(workMin, shortMin + delta, longMin)
    fun changeLong(delta: Int) = setDurations(workMin, shortMin, longMin + delta)

    fun toggle(onPhaseEnd: (PomoPhase) -> Unit) =
        if (running) pause() else start(onPhaseEnd)

    fun start(onPhaseEnd: (PomoPhase) -> Unit) {
        if (running || remaining <= 0) return
        running = true
        endAt = SystemClock.elapsedRealtime() + remaining
        val ctx = getApplication<Application>()
        TimerService.startCountdown(
            ctx,
            TimerService.KEY_POMODORO,
            endAt,
            title = ctx.getString(R.string.pomodoro_phase_title, ctx.getString(phase.labelRes())),
            finishTitle = ctx.getString(R.string.pomodoro),
            finishMessage = messageFor(nextPhasePreview()),
            finishAlertId = 1002
        )
        job = viewModelScope.launch {
            while (isActive) {
                remaining = (endAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)
                if (remaining <= 0) {
                    advancePhase(onPhaseEnd)
                    break
                }
                delay(50)
            }
        }
    }

    /** The phase that will start once the current one ends (no state change). */
    private fun nextPhasePreview(): PomoPhase = if (phase == PomoPhase.WORK) {
        if ((completedSessions + 1) % cyclesBeforeLong == 0) PomoPhase.LONG_BREAK
        else PomoPhase.SHORT_BREAK
    } else {
        PomoPhase.WORK
    }

    private fun messageFor(next: PomoPhase): String {
        val ctx = getApplication<Application>()
        return when (next) {
            PomoPhase.WORK -> ctx.getString(R.string.pomo_msg_focus)
            PomoPhase.SHORT_BREAK -> ctx.getString(R.string.pomo_msg_short)
            PomoPhase.LONG_BREAK -> ctx.getString(R.string.pomo_msg_long)
        }
    }

    private fun advancePhase(onPhaseEnd: (PomoPhase) -> Unit) {
        running = false
        val ended = phase
        phase = if (ended == PomoPhase.WORK) {
            completedSessions++
            if (completedSessions % cyclesBeforeLong == 0) PomoPhase.LONG_BREAK else PomoPhase.SHORT_BREAK
        } else {
            PomoPhase.WORK
        }
        remaining = phaseTotalMillis()
        onPhaseEnd(phase)
    }

    fun pause() {
        running = false
        job?.cancel()
        TimerService.stop(getApplication<Application>(), TimerService.KEY_POMODORO)
    }

    fun reset() {
        pause()
        remaining = phaseTotalMillis()
    }

    fun skip(onPhaseEnd: (PomoPhase) -> Unit) {
        pause()
        advancePhase(onPhaseEnd)
    }

    fun resetAll() {
        pause()
        phase = PomoPhase.WORK
        completedSessions = 0
        remaining = phaseTotalMillis()
    }

    override fun onCleared() {
        job?.cancel()
    }
}
