package com.dmitrysukhov.lifetracker.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleNotification(taskId: Long, taskTitle: String, notificationTime: Long) {
        cancelNotification(taskId)
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = notificationTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val adjustedNotificationTime = calendar.timeInMillis
        
        val currentTime = System.currentTimeMillis()
        if (adjustedNotificationTime <= currentTime) {
            return
        }
        
        val timeUntilNotification = adjustedNotificationTime - currentTime
        val timeUntilNotificationMinutes = timeUntilNotification / (60 * 1000)
        
        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            action = "com.dmitrysukhov.lifetracker.NOTIFICATION_$taskId"
            addCategory(Intent.CATEGORY_DEFAULT)
            
            putExtra(TodoReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TodoReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra("scheduled_time", adjustedNotificationTime)
            putExtra("scheduled_at", System.currentTimeMillis())
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (canScheduleExact && timeUntilNotificationMinutes <= 24 * 60) {
                try {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        adjustedNotificationTime,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    return
                } catch (_: Exception) {
                }
            }

            try {
                if (canScheduleExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        adjustedNotificationTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        adjustedNotificationTime,
                        pendingIntent
                    )
                }
                return
            } catch (_: Exception) {
            }

            try {
                if (canScheduleExact) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        adjustedNotificationTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        adjustedNotificationTime,
                        pendingIntent
                    )
                }
                return
            } catch (_: Exception) {
            }
            
            alarmManager.set(AlarmManager.RTC_WAKEUP, adjustedNotificationTime, pendingIntent)
            
        } catch (_: Exception) {
        }
        
        try {
            val backupIntent = Intent(context, TodoReminderReceiver::class.java).apply {
                action = "com.dmitrysukhov.lifetracker.BACKUP_NOTIFICATION_$taskId"
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra(TodoReminderReceiver.EXTRA_TASK_ID, taskId)
                putExtra(TodoReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
                putExtra("scheduled_time", adjustedNotificationTime)
                putExtra("is_backup", true)
            }
            
            val backupPendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt() + 10000,
                backupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                adjustedNotificationTime + 60 * 1000,
                backupPendingIntent
            )
        } catch (_: Exception) {
        }
    }
    
    fun cancelNotification(taskId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, TodoReminderReceiver::class.java).apply {
                action = "com.dmitrysukhov.lifetracker.NOTIFICATION_$taskId"
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                intent, 
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            
            val backupIntent = Intent(context, TodoReminderReceiver::class.java).apply {
                action = "com.dmitrysukhov.lifetracker.BACKUP_NOTIFICATION_$taskId"
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            
            val backupPendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt() + 10000,
                backupIntent, 
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (backupPendingIntent != null) {
                alarmManager.cancel(backupPendingIntent)
                backupPendingIntent.cancel()
            }
        } catch (_: Exception) {
        }
    }
    
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    fun getAlarmPermissionSettingsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }
} 