package com.dmitrysukhov.lifetracker.todo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dmitrysukhov.lifetracker.MainActivity
import com.dmitrysukhov.lifetracker.R

class TodoReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "todo_reminders"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        var wakeLock: PowerManager.WakeLock? = null
        
        try {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "lifetracker:reminder_wakelock"
                )
                wakeLock.acquire(60000)
            } catch (_: Exception) {
            }
            
            processNotification(context, intent)
            
        } finally {
            try {
                if (wakeLock != null && wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (_: Exception) {
            }
        }
    }
    
    private fun processNotification(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
        val scheduledTime = intent.getLongExtra("scheduled_time", 0L)
        
        if (taskId <= 0 || taskTitle.isEmpty() || scheduledTime <= 0) {
            return
        }
        
        try {
            createNotificationChannel(context)
            showNotification(context, taskId, taskTitle)
        } catch (_: Exception) {
            try {
                showAlternativeNotification(context, taskId, taskTitle)
            } catch (_: Exception) {
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.task_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.task_reminders_desc)
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, taskId: Long, taskTitle: String) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.lifetracker_monochrome)
            .setContentTitle(taskTitle)
            .setContentText(context.getString(R.string.task_reminder))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(taskTitle)
                .setSummaryText(context.getString(R.string.task_reminder)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        with(NotificationManagerCompat.from(context)) {
            notify(taskId.toInt(), builder.build())
        }
    }
    
    private fun showAlternativeNotification(context: Context, taskId: Long, taskTitle: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.lifetracker_monochrome)
            .setContentTitle(taskTitle)
            .setContentText(context.getString(R.string.task_reminder))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            
        notificationManager.notify(taskId.toInt() + 1000, builder.build())
    }
} 