package com.odak.app.timer

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.odak.app.R
import com.odak.app.util.Alert
import com.odak.app.util.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Foreground service that shows a live, ongoing notification while a timer,
 * pomodoro phase or stopwatch is running — so the remaining/elapsed time stays
 * visible even when the app is in the background. Countdown entries fire the
 * end-of-time [Alert] themselves, which keeps the alarm reliable if the app's
 * UI process is no longer ticking.
 */
class TimerService : Service() {

    private enum class Mode { COUNTDOWN, STOPWATCH }

    private data class Entry(
        val key: Int,
        val mode: Mode,
        /** COUNTDOWN: end time; STOPWATCH: start time — both in elapsedRealtime. */
        val anchor: Long,
        val title: String,
        val finishTitle: String,
        val finishMessage: String,
        val finishAlertId: Int
    )

    private val entries = ConcurrentHashMap<Int, Entry>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ticker: Job? = null
    private var foregroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val key = intent.getIntExtra(EXTRA_KEY, 0)
                entries[key] = Entry(
                    key = key,
                    mode = if (intent.getBooleanExtra(EXTRA_COUNT_UP, false)) Mode.STOPWATCH
                    else Mode.COUNTDOWN,
                    anchor = intent.getLongExtra(EXTRA_ANCHOR, 0L),
                    title = intent.getStringExtra(EXTRA_TITLE) ?: "Zamanlayıcı",
                    finishTitle = intent.getStringExtra(EXTRA_FINISH_TITLE) ?: "Süre doldu",
                    finishMessage = intent.getStringExtra(EXTRA_FINISH_MESSAGE) ?: "",
                    finishAlertId = intent.getIntExtra(EXTRA_FINISH_ID, 1000)
                )
                ensureForeground()
                startTicking()
            }

            ACTION_STOP -> {
                val key = intent.getIntExtra(EXTRA_KEY, 0)
                entries.remove(key)
                if (entries.isEmpty()) stopEverything() else refreshNotification()
            }
        }
        // Nothing to resume from a blank restart, so don't re-deliver.
        return START_NOT_STICKY
    }

    private fun ensureForeground() {
        val notif = buildNotification()
        if (!foregroundStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(FOREGROUND_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(FOREGROUND_ID, notif)
            }
            foregroundStarted = true
        } else {
            refreshNotification()
        }
    }

    private fun startTicking() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            while (isActive) {
                if (tick()) break
                delay(1000)
            }
        }
    }

    /** Updates notifications and fires finished countdowns. Returns true when idle. */
    private fun tick(): Boolean {
        val now = SystemClock.elapsedRealtime()
        entries.values
            .filter { it.mode == Mode.COUNTDOWN && it.anchor - now <= 0L }
            .forEach { e ->
                entries.remove(e.key)
                Alert.fire(this, e.finishTitle, e.finishMessage, e.finishAlertId)
            }
        if (entries.isEmpty()) {
            stopEverything()
            return true
        }
        refreshNotification()
        return false
    }

    private fun refreshNotification() {
        runCatching {
            NotificationManagerCompat.from(this).notify(FOREGROUND_ID, buildNotification())
        }
    }

    private fun buildNotification(): Notification {
        val now = SystemClock.elapsedRealtime()
        val lines = entries.values.map { e ->
            val time = when (e.mode) {
                Mode.COUNTDOWN -> TimeFormat.clock((e.anchor - now).coerceAtLeast(0))
                Mode.STOPWATCH -> TimeFormat.hms((now - e.anchor).coerceAtLeast(0))
            }
            "${e.title}: $time"
        }
        val single = lines.size == 1
        val title = if (single) entries.values.first().title else "Zamanlayıcılar çalışıyor"
        val text = lines.joinToString("\n")
        return NotificationCompat.Builder(this, Alert.ONGOING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText(if (single) lines.first().substringAfter(": ") else text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(Alert.openAppIntent(this))
            .build()
    }

    private fun stopEverything() {
        ticker?.cancel()
        ticker = null
        foregroundStarted = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val FOREGROUND_ID = 2001
        const val KEY_COUNTDOWN = 1
        const val KEY_POMODORO = 2
        const val KEY_STOPWATCH = 3

        private const val ACTION_START = "com.odak.app.timer.START"
        private const val ACTION_STOP = "com.odak.app.timer.STOP"
        private const val EXTRA_KEY = "key"
        private const val EXTRA_ANCHOR = "anchor"
        private const val EXTRA_COUNT_UP = "count_up"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_FINISH_TITLE = "finish_title"
        private const val EXTRA_FINISH_MESSAGE = "finish_message"
        private const val EXTRA_FINISH_ID = "finish_id"

        fun startCountdown(
            context: Context,
            key: Int,
            endAtElapsed: Long,
            title: String,
            finishTitle: String,
            finishMessage: String,
            finishAlertId: Int
        ) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_KEY, key)
                putExtra(EXTRA_ANCHOR, endAtElapsed)
                putExtra(EXTRA_COUNT_UP, false)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_FINISH_TITLE, finishTitle)
                putExtra(EXTRA_FINISH_MESSAGE, finishMessage)
                putExtra(EXTRA_FINISH_ID, finishAlertId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun startStopwatch(context: Context, key: Int, baseElapsed: Long, title: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_KEY, key)
                putExtra(EXTRA_ANCHOR, baseElapsed)
                putExtra(EXTRA_COUNT_UP, true)
                putExtra(EXTRA_TITLE, title)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context, key: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_KEY, key)
            }
            runCatching { context.startService(intent) }
        }
    }
}
