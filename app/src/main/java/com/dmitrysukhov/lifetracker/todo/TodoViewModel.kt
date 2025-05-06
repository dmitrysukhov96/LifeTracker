package com.dmitrysukhov.lifetracker.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import com.dmitrysukhov.lifetracker.tracker.EventRepository
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
    private val eventRepository: EventRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var selectedTask: TodoItem? = null
    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _currentlyTrackedTask = MutableStateFlow<TodoItem?>(null)

    private val _lastEvent = MutableStateFlow<Event?>(null)

    @Inject
    lateinit var projectsDao: ProjectDao

    init {
        loadTasks()
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            loadProjects()
            
            eventRepository.getLastEvent().collect { event ->
                _lastEvent.value = event
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            try {
                projectsDao.getAllProjects().collectLatest { list ->
                    _projects.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasks = todoDao.getAllTasks()
            val sortedTasks = sortTasks(tasks)
            _todoList.value = sortedTasks
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
                task.dateTime == null -> 5
                task.dateTime < yesterdayStart -> 1
                task.dateTime < todayStart -> 2
                task.dateTime < tomorrowStart -> 3
                task.dateTime < tomorrowStart + (24 * 60 * 60 * 1000) -> 4
                else -> 6
            }
        }.thenBy { it.dateTime ?: Long.MAX_VALUE })
        return sortedNotDone + done.sortedByDescending { it.dateTime }
    }

    fun startTracking(task: TodoItem) {
        if (task.estimatedDurationMs == null || task.estimatedDurationMs <= 0) return
        
        stopExistingTracking()
        
        val now = System.currentTimeMillis()
        val newEvent = Event(
            eventId = 0L,
            projectId = task.projectId,
            name = task.text,
            startTime = now,
            endTime = null
        )
        
        _currentlyTrackedTask.value = task
        
        viewModelScope.launch {
            eventRepository.insertEvent(newEvent)
            loadTasks()
        }
    }
    
    private fun stopExistingTracking() {
        val existingEvent = _lastEvent.value
        if (existingEvent != null && existingEvent.endTime == null) {
            val updatedEvent = existingEvent.copy(endTime = System.currentTimeMillis())
            viewModelScope.launch {
                eventRepository.updateEvent(updatedEvent)
            }
        }
        
        _currentlyTrackedTask.value = null
    }
    
    fun stopTracking() {
        stopExistingTracking()
    }

    fun addTask(
        text: String, description: String = "", projectId: Long? = null, deadline: Long? = null,
        repeatInterval: String? = null, estimatedDurationMs: Long? = null
    ) {
        val newTask = TodoItem(
            text = text, description = description, projectId = projectId, dateTime = deadline,
            repeatInterval = repeatInterval, estimatedDurationMs = estimatedDurationMs,
            isDone = false
        )
        viewModelScope.launch {
            val id = todoDao.insertTask(newTask)
            loadTasks()
            
            deadline?.let {
                scheduleNotification(id, text, it)
            }
        }
    }

    fun updateTask(item: TodoItem) {
        viewModelScope.launch {
            todoDao.updateTask(item)
            loadTasks()
            
            if (_currentlyTrackedTask.value?.id == item.id) {
                _currentlyTrackedTask.value = item
            }
            
            item.dateTime?.let {
                cancelNotification(item.id)
                scheduleNotification(item.id, item.text, it)
            } ?: run {
                cancelNotification(item.id)
            }
        }
    }

    fun deleteTask(item: TodoItem) {
        if (_currentlyTrackedTask.value?.id == item.id) {
            stopTracking()
        }
        
        viewModelScope.launch {
            todoDao.deleteTask(item)
            loadTasks()
            
            cancelNotification(item.id)
        }
    }
    
    private fun scheduleNotification(taskId: Long, taskTitle: String, notificationTime: Long) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = notificationTime
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val adjustedNotificationTime = calendar.timeInMillis
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TodoReminderReceiver::class.java).apply {
            putExtra(TodoReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TodoReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (adjustedNotificationTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    adjustedNotificationTime,
                    pendingIntent
                )
            } catch (_: Exception) {
            }
        }
    }
    
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

    fun stopTrackingWithTask(updatedTask: TodoItem) {
        val existingEvent = _lastEvent.value
        if (existingEvent != null && existingEvent.endTime == null) {
            val updatedEvent = existingEvent.copy(endTime = System.currentTimeMillis())
            viewModelScope.launch {
                eventRepository.updateEvent(updatedEvent)
            }
        }
        
        _currentlyTrackedTask.value = null
        
        viewModelScope.launch {
            todoDao.updateTask(updatedTask)
            loadTasks()
            println("Updated task ${updatedTask.id} with explicit duration: ${updatedTask.estimatedDurationMs}ms")
        }
    }
}
