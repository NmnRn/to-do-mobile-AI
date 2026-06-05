package com.odak.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.odak.app.MainActivity
import com.odak.app.R

/**
 * Fires an end-of-timer alert: plays a sound, vibrates and posts a heads-up
 * notification. Sound + vibration are always triggered directly so the user is
 * alerted even when the app is in the foreground (where the channel sound may
 * stay silent) or when notifications are blocked.
 */
object Alert {
    const val CHANNEL_ID = "odak_alerts"
    const val REMINDER_CHANNEL_ID = "odak_reminders"
    const val ONGOING_CHANNEL_ID = "odak_ongoing"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        ensureAlertChannel(manager)
        ensureReminderChannel(manager)
        ensureOngoingChannel(manager)
    }

    private fun ensureAlertChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Zamanlayıcı & Pomodoro",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Süre dolduğunda sesli ve titreşimli bildirim"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            setSound(sound, attrs)
        }
        manager.createNotificationChannel(channel)
    }

    private fun ensureReminderChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Görev hatırlatmaları",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Bekleyen görevler için düzenli hatırlatmalar"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    private fun ensureOngoingChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(ONGOING_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            ONGOING_CHANNEL_ID,
            "Çalışan zamanlayıcı",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Zamanlayıcı/pomodoro çalışırken kalıcı bildirim"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    /** Intent that opens the app when a notification is tapped. */
    fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Posts a non-intrusive reminder about pending tasks. */
    fun fireReminder(context: Context, title: String, message: String) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REMINDER_NOTIF_ID, notification)
    }

    const val REMINDER_NOTIF_ID = 3001

    fun fire(context: Context, title: String, message: String, id: Int) {
        ensureChannel(context)

        Buzz.vibrate(context, 700)
        playSound(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun playSound(context: Context) {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            RingtoneManager.getRingtone(context, uri)?.play()
        }
    }
}
