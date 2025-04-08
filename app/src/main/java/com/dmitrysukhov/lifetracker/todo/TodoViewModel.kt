package com.dmitrysukhov.lifetracker.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoDao: TodoDao
) : ViewModel() {
    var selectedTask: TodoItem? = null
    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _todoList.value = todoDao.getAllTasks()
        }
    }

    fun addTask(text: String) {
        val newTask = TodoItem(
            text = text,
            description = listOf(
                "Проверить почту",
                "Позвонить клиенту",
                "Прочитать статью",
                "Почистить входящие",
                "Повторить материал"
            ).random(),
            projectId = listOf(1L, 2L, 3L, null).random(), // пример: случайный проект или без проекта
            dateTime = System.currentTimeMillis() + (1..7).random() * 24 * 60 * 60 * 1000L, // через 1–7 дней
            reminderTime = System.currentTimeMillis() + (1..6).random() * 60 * 60 * 1000L, // через 1–6 часов
            repeatInterval = listOf("daily", "weekly", null).random(),
            durationMinutes = listOf(15, 30, 60, 90, 120).random(),
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

//    fun deleteTask(item: TodoItem) {
//        viewModelScope.launch {
//            todoDao.deleteTask(item)
//            loadTasks()
//        }
//    }
}
