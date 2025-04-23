package com.dmitrysukhov.lifetracker.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoDao: TodoDao
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
            _todoList.value = todoDao.getAllTasks()
        }
    }

    fun addTask(
        text: String,
        description: String = "",
        projectId: Long? = null,
        deadline: Long? = null
    ) {
        val newTask = TodoItem(
            text = text,
            description = description,
            projectId = projectId,
            dateTime = (deadline ?: System.currentTimeMillis()) + 24 * 60 * 60 * 1000L, // default: tomorrow
            reminderTime = null,
            repeatInterval = null,
            durationMinutes = null,
            isDone = false
        )

        viewModelScope.launch {
            todoDao.insertTask(newTask)
            loadTasks()
        }
    }

    fun updateTask(item: TodoItem) {
        viewModelScope.launch {
            todoDao.updateTask(item)
            loadTasks()
        }
    }

    fun deleteTask(item: TodoItem) {
        viewModelScope.launch {
            todoDao.deleteTask(item)
            loadTasks()
        }
    }
}
