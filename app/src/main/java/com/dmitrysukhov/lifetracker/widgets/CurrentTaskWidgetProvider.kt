package com.dmitrysukhov.lifetracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.widget.RemoteViews
import com.dmitrysukhov.lifetracker.MainActivity
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.tracker.EventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.Application
import android.content.SharedPreferences
import androidx.room.Room
import com.dmitrysukhov.lifetracker.AppDatabase
import com.dmitrysukhov.lifetracker.projects.ProjectRepository
import com.dmitrysukhov.lifetracker.projects.ProjectRepositoryImpl

@AndroidEntryPoint
class CurrentTaskWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var eventRepository: EventRepository

    companion object {
        const val ACTION_WIDGET_UPDATE = "com.dmitrysukhov.lifetracker.widgets.ACTION_WIDGET_UPDATE"
        
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CurrentTaskWidgetProvider::class.java)
            intent.action = ACTION_WIDGET_UPDATE
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_WIDGET_UPDATE || 
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateWidgets(context)
        }
    }

    private fun updateWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, CurrentTaskWidgetProvider::class.java)
        )
        
        if (appWidgetIds.isNotEmpty()) {
            updateAppWidgets(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
    }
    
    private fun updateAppWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the current task
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifetracker_database"
                ).build()
                
                val eventDao = db.eventDao()
                val repository = EventRepository(eventDao)
                val lastEvent = repository.getLastEvent().first()
                
                // Only consider the task active if it exists and doesn't have an end time
                val currentTask = if (lastEvent != null && lastEvent.endTime == null) lastEvent else null
                
                // Get project info if task has a project associated
                var projectName = ""
                if (currentTask?.projectId != null) {
                    val projectDao = db.projectsDao()
                    val projectRepository = ProjectRepositoryImpl(projectDao)
                    val projects = projectRepository.getAllProjects().first()
                    val project = projects.find { it.projectId == currentTask.projectId }
                    projectName = project?.title ?: ""
                }
                
                // Update all widgets
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, currentTask?.name, projectName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // In case of error, show "No task" for all widgets
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, null, "")
                }
            }
        }
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        taskName: String?,
        projectName: String
    ) {
        // Create RemoteViews for our widget layout
        val views = RemoteViews(context.packageName, R.layout.widget_current_task)
        
        // Set task text
        val displayText = taskName ?: context.getString(R.string.no_task)
        views.setTextViewText(R.id.widget_task_name, displayText)
        
        // Set project name if available
        if (projectName.isNotEmpty()) {
            views.setTextViewText(R.id.widget_project_name, projectName)
            views.setViewVisibility(R.id.widget_project_name, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_project_name, android.view.View.GONE)
        }
        
        // Create intent to open app when clicking on task text
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            // Add flags to clear back stack and start as new task
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingOpenAppIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                context, 
                0, 
                openAppIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context, 
                0, 
                openAppIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        views.setOnClickPendingIntent(R.id.widget_task_container, pendingOpenAppIntent)
        
        // Create intent to refresh widget when clicking on "NOW:" label
        val refreshIntent = Intent(context, CurrentTaskWidgetProvider::class.java)
        refreshIntent.action = ACTION_WIDGET_UPDATE
        val pendingRefreshIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context, 
                0, 
                refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context, 
                0, 
                refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        views.setOnClickPendingIntent(R.id.widget_now_label, pendingRefreshIntent)
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
} 