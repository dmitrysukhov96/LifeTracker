package com.dmitrysukhov.lifetracker.todo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var todoDao: TodoDao
    
    @Inject
    lateinit var notificationManager: TodoNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tasks = todoDao.getTasksWithDeadlines()
                    
                    val currentTime = System.currentTimeMillis()
                    
                    tasks.forEach { task ->
                        if (task.dateTime != null && task.dateTime > currentTime) {
                            notificationManager.scheduleNotification(task.id, task.text, task.dateTime)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
} 