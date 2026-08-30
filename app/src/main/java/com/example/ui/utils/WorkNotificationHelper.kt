package com.example.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object WorkNotificationHelper {
    private const val CHANNEL_ID = "work_tracking_notifications"
    private const val CHANNEL_NAME = "Work & Reminders"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PAUSE = "com.example.ACTION_TIMER_PAUSE"
    const val ACTION_RESUME = "com.example.ACTION_TIMER_RESUME"
    const val ACTION_STOP = "com.example.ACTION_TIMER_STOP"

    private val _timerEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val timerEvents: SharedFlow<String> = _timerEvents.asSharedFlow()

    fun sendTimerEvent(action: String) {
        _timerEvents.tryEmit(action)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for work timers, labor costs, and task reminders"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int = NOTIFICATION_ID) {
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showTimerOngoingNotification(
        context: Context,
        projectName: String,
        formattedTime: String,
        accruedCost: String,
        isRunning: Boolean
    ) {
        createNotificationChannel(context)

        val pauseResumeActionIntent = Intent(context, TimerActionReceiver::class.java).apply {
            action = if (isRunning) ACTION_PAUSE else ACTION_RESUME
        }
        val pauseResumePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseResumeActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = Intent(context, TimerActionReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Tracking: $projectName")
            .setContentText("Time: $formattedTime • Cost: $accruedCost")
            .setOngoing(isRunning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .addAction(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isRunning) "Pause" else "Resume",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "Stop & Save",
                stopPendingIntent
            )

        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(context: Context, notificationId: Int = NOTIFICATION_ID) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
