package com.dmitrysukhov.lifetracker.todo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Starting TodoNotificationService")
            val serviceIntent = Intent(context, TodoNotificationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                    Log.d(TAG, "Started as foreground service")
                } else {
                    context.startService(serviceIntent)
                    Log.d(TAG, "Started as regular service")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
            }
        }
    }
} 