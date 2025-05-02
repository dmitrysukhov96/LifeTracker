package com.dmitrysukhov.lifetracker.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.TodoItem
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
            val tasks = todoDao.getAllTasks()
            _todoList.value = sortTasks(tasks)
        }
    }

    private fun sortTasks(tasks: List<TodoItem>): List<TodoItem> {
        val now = System.currentTimeMillis()
        val today = now - (now % (24 * 60 * 60 * 1000))
        val yesterday = today - (24 * 60 * 60 * 1000)
        val tomorrow = today + (24 * 60 * 60 * 1000)

        // Разделяем задачи на выполненные и невыполненные
        val (done, notDone) = tasks.partition { it.isDone }

        // Сортируем невыполненные задачи по временным категориям
        val sortedNotDone = notDone.sortedWith(compareBy<TodoItem> { task ->
            when {
                task.dateTime == null -> 5 // Без даты в конце невыполненных
                task.dateTime < yesterday -> 1 // Ранее
                task.dateTime < today -> 2 // Вчера
                task.dateTime < tomorrow -> 3 // Сегодня
                task.dateTime < tomorrow + (24 * 60 * 60 * 1000) -> 4 // Завтра
                else -> 5 // Позже
            }
        }.thenBy { it.dateTime ?: Long.MAX_VALUE })

        // Объединяем отсортированные невыполненные задачи с выполненными
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
            dateTime = (deadline ?: System.currentTimeMillis()) + 24 * 60 * 60 * 1000L, // default: tomorrow
            reminderTime = reminderTime,
            repeatInterval = repeatInterval,
            durationMinutes = durationMinutes,
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
