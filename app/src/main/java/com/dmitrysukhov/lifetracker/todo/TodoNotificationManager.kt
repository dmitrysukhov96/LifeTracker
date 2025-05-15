package com.dmitrysukhov.lifetracker.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Singleton
class TodoNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TodoNotificationManager"
    }

    fun scheduleNotification(taskId: Long, taskTitle: String, notificationTime: Long) {
        // Ensure we're working with UTC time to avoid timezone issues
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = notificationTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val adjustedNotificationTime = calendar.timeInMillis
        
        // Log the timing details for debugging
        val currentTime = System.currentTimeMillis()
        val timeUntilNotification = adjustedNotificationTime - currentTime
        Log.d(TAG, "Scheduling notification for task $taskId:")
        Log.d(TAG, "Current time: ${Date(currentTime)}")
        Log.d(TAG, "Notification time: ${Date(adjustedNotificationTime)}")
        Log.d(TAG, "Time until notification: ${timeUntilNotification / 1000} seconds")
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Проверяем, поддерживает ли устройство точные будильники
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Device doesn't support exact alarms")
                return
            }
        }
        
        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            putExtra(TodoReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TodoReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            // Add the exact notification time to the intent for verification
            putExtra("scheduled_time", adjustedNotificationTime)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (adjustedNotificationTime > currentTime) {
            try {
                // Use setExactAndAllowWhileIdle for more precise timing
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    adjustedNotificationTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for task $taskId at ${Date(adjustedNotificationTime)}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule exact alarm", e)
                // Fallback to setAlarmClock if setExactAndAllowWhileIdle fails
                try {
                    val alarmInfo = AlarmManager.AlarmClockInfo(adjustedNotificationTime, pendingIntent)
                    alarmManager.setAlarmClock(alarmInfo, pendingIntent)
                    Log.d(TAG, "Fallback to setAlarmClock for task $taskId")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to schedule fallback alarm", e)
                }
            }
        } else {
            Log.w(TAG, "Notification time $adjustedNotificationTime is in the past, not scheduling")
        }
    }
    
    fun cancelNotification(taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TodoReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent, 
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled alarm for task $taskId")
        }
    }
} 