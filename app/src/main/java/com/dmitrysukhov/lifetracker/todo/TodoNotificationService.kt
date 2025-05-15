package com.dmitrysukhov.lifetracker.todo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dmitrysukhov.lifetracker.MainActivity
import com.dmitrysukhov.lifetracker.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TodoNotificationService : Service() {
    companion object {
        private const val TAG = "TodoNotificationService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "notification_service_channel"
    }

    @Inject
    lateinit var todoDao: TodoDao
    
    @Inject
    lateinit var notificationManager: TodoNotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        try {
            createNotificationChannel()
            // Start foreground immediately with a temporary notification
            startForeground(NOTIFICATION_ID, createTemporaryNotification())
            checkAndUpdateServiceState()
            Log.d(TAG, "Service started")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        try {
            // Start foreground immediately with a temporary notification
            startForeground(NOTIFICATION_ID, createTemporaryNotification())
            checkAndUpdateServiceState()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        Log.d(TAG, "Creating notification channel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.task_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.task_reminders_desc)
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createTemporaryNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.task_reminders_desc))
        .setSmallIcon(R.drawable.bell)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun checkAndUpdateServiceState() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = todoDao.getTasksWithDeadlines()
                val hasActiveNotifications = tasks.any { 
                    it.dateTime != null && it.dateTime > System.currentTimeMillis() 
                }

                if (hasActiveNotifications) {
                    updateForegroundNotification()
                    Log.d(TAG, "Service running in foreground with active notifications")
                } else {
                    stopForeground(true)
                    stopSelf()
                    Log.d(TAG, "No active notifications, stopping service")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking service state", e)
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun updateForegroundNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = todoDao.getTasksWithDeadlines()
                val nextTask = tasks
                    .filter { it.dateTime != null && it.dateTime > System.currentTimeMillis() }
                    .minByOrNull { it.dateTime ?: Long.MAX_VALUE }

                if (nextTask != null) {
                    val notification = NotificationCompat.Builder(this@TodoNotificationService, CHANNEL_ID)
                        .setContentTitle(nextTask.text)
                        .setContentText(getString(R.string.task_reminder))
                        .setSmallIcon(R.drawable.bell)
                        .setContentIntent(
                            PendingIntent.getActivity(
                                this@TodoNotificationService,
                                0,
                                Intent(this@TodoNotificationService, MainActivity::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                        .build()
                    startForeground(NOTIFICATION_ID, notification)
                    Log.d(TAG, "Updated foreground notification with next task: ${nextTask.text}")
                } else {
                    stopForeground(true)
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating foreground notification", e)
                stopForeground(true)
                stopSelf()
            }
        }
    }
} 