package com.dmitrysukhov.lifetracker.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoDao: TodoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var selectedTask: TodoItem? = null
    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    @Inject
    lateinit var projectsDao: ProjectDao

    init {
        loadTasks()
        viewModelScope.launch {
            // Defer projects loading to ensure projectsDao is injected
            kotlinx.coroutines.delay(100)
            loadProjects()
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                projectsDao.getAllProjects().collectLatest { list ->
                    _projects.value = list
                }
            } catch (e: Exception) {
                // Handle case when projectsDao is not yet initialized
                e.printStackTrace()
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasks = todoDao.getAllTasks()
            _todoList.value = sortTasks(tasks)
        }
    }

    private fun sortTasks(tasks: List<TodoItem>): List<TodoItem> {
        System.currentTimeMillis()

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)
        val tomorrowStart = todayStart + (24 * 60 * 60 * 1000)
        tasks.take(3).forEach { task -> }
        val (done, notDone) = tasks.partition { it.isDone }
        val sortedNotDone = notDone.sortedWith(compareBy<TodoItem> { task ->
            when {
                task.dateTime == null -> 5 // Без даты
                task.dateTime < yesterdayStart -> 1 // Ранее
                task.dateTime < todayStart -> 2 // Вчера (задачи из вчерашнего дня)
                task.dateTime < tomorrowStart -> 3 // Сегодня (задачи из текущего дня)
                task.dateTime < tomorrowStart + (24 * 60 * 60 * 1000) -> 4 // Завтра
                else -> 6 // Позже
            }
        }.thenBy { it.dateTime ?: Long.MAX_VALUE })
        return sortedNotDone + done.sortedByDescending { it.dateTime }
    }

    fun addTask(
        text: String,
        description: String = "",
        projectId: Long? = null,
        deadline: Long? = null,
        reminderTime: Long? = null,
        repeatInterval: String? = null,
        durationMinutes: Int? = null
    ) {
        val newTask = TodoItem(
            text = text,
            description = description,
            projectId = projectId,
            dateTime = deadline,
            reminderTime = reminderTime,
            repeatInterval = repeatInterval,
            durationMinutes = durationMinutes,
            isDone = false
        )

        viewModelScope.launch {
            val id = todoDao.insertTask(newTask)
            loadTasks()
            
            // Установка напоминания
            reminderTime?.let {
                scheduleNotification(id, text, it)
            }
        }
    }

    fun updateTask(item: TodoItem) {
        viewModelScope.launch {
            todoDao.updateTask(item)
            loadTasks()
            
            // Обновляем напоминание
            item.reminderTime?.let {
                // Сначала удаляем старое напоминание
                cancelNotification(item.id)
                // Затем устанавливаем новое
                scheduleNotification(item.id, item.text, it)
            } ?: run {
                // Если напоминания нет, удаляем существующее (если было)
                cancelNotification(item.id)
            }
        }
    }

    fun deleteTask(item: TodoItem) {
        viewModelScope.launch {
            todoDao.deleteTask(item)
            loadTasks()
            
            // Удаляем напоминание
            cancelNotification(item.id)
        }
    }
    
    // Метод для установки напоминания через AlarmManager
    private fun scheduleNotification(taskId: Long, taskTitle: String, reminderTime: Long) {
        // Set seconds to 0 for the reminder time
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminderTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val adjustedReminderTime = calendar.timeInMillis
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Создаем Intent для нашего BroadcastReceiver
        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            putExtra(TodoReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TodoReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        
        // Уникальный запрос для каждой задачи, чтобы мы могли управлять ими индивидуально
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),  // используем ID задачи как requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Устанавливаем точное время срабатывания будильника
        if (adjustedReminderTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    adjustedReminderTime,
                    pendingIntent
                )
            } catch (_: Exception) {
                // Failed to schedule alarm
            }
        }
    }
    
    // Метод для отмены напоминания
    private fun cancelNotification(taskId: Long) {
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
        }
    }
}
