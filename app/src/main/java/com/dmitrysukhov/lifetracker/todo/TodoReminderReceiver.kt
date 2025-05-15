package com.dmitrysukhov.lifetracker.todo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dmitrysukhov.lifetracker.MainActivity
import com.dmitrysukhov.lifetracker.R
import java.util.*

class TodoReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "todo_reminders"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
        val scheduledTime = intent.getLongExtra("scheduled_time", 0L)
        
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - scheduledTime
        
        Log.d("TodoReminderReceiver", "Received notification for task $taskId:")
        Log.d("TodoReminderReceiver", "Scheduled time: ${Date(scheduledTime)}")
        Log.d("TodoReminderReceiver", "Current time: ${Date(currentTime)}")
        Log.d("TodoReminderReceiver", "Time difference: ${timeDiff / 1000} seconds")
        
        // Only show notification if we're within 5 minutes of the scheduled time
        // This helps prevent notifications from being triggered too early
        if (Math.abs(timeDiff) <= 5 * 60 * 1000) {
            createNotificationChannel(context)
            showNotification(context, taskId, taskTitle)
        } else {
            Log.w("TodoReminderReceiver", "Notification received too early/late, ignoring")
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
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, taskId: Long, taskTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.bell)
            .setContentTitle(taskTitle)
            .setContentText(context.getString(R.string.task_reminder))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(taskTitle)
                .setSummaryText(context.getString(R.string.task_reminder)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.toInt(), builder.build())
    }
} 