package com.dmitrysukhov.lifetracker.todo

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import com.dmitrysukhov.lifetracker.tracker.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoDao: TodoDao,
    private val eventRepository: EventRepository,
    private val notificationManager: TodoNotificationManager
) : ViewModel() {
    var selectedTask: TodoItem? = null
    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _currentlyTrackedTask = MutableStateFlow<TodoItem?>(null)

    private val _lastEvent = MutableStateFlow<Event?>(null)
    
    private val _needsPermission = MutableStateFlow(false)

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
        
        checkAlarmPermission()
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
            
            if (deadline != null && deadline > System.currentTimeMillis()) {
                if (checkAlarmPermission()) {
                    scheduleNotification(id, text, deadline)
                } else {
                    _needsPermission.value = true
                }
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
            
            cancelNotification(item.id)
            
            if (item.dateTime != null && item.dateTime > System.currentTimeMillis()) {
                if (checkAlarmPermission()) {
                    scheduleNotification(item.id, item.text, item.dateTime)
                } else {
                    _needsPermission.value = true
                }
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
    
    fun scheduleNotification(taskId: Long, taskTitle: String, notificationTime: Long) {
        notificationManager.scheduleNotification(taskId, taskTitle, notificationTime)
    }
    
    private fun cancelNotification(taskId: Long) {
        notificationManager.cancelNotification(taskId)
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
        }
    }
    
    fun checkAlarmPermission(): Boolean {
        val hasPermission = notificationManager.hasExactAlarmPermission()
        _needsPermission.value = !hasPermission
        return hasPermission
    }
    
    fun getAlarmPermissionSettingsIntent(): Intent? {
        return notificationManager.getAlarmPermissionSettingsIntent()
    }
    
    fun onPermissionGranted() {
        _needsPermission.value = false
        
        viewModelScope.launch {
            val tasks = todoDao.getTasksWithDeadlines()
            val currentTime = System.currentTimeMillis()
            
            tasks.forEach { task ->
                if (task.dateTime != null && task.dateTime > currentTime) {
                    scheduleNotification(task.id, task.text, task.dateTime)
                }
            }
        }
    }
}
