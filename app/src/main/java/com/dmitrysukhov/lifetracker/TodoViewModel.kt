package com.dmitrysukhov.lifetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodoViewModel(private val todoDao: TodoDao) : ViewModel() {
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
        val newTask = TodoItem(text = text, isDone = false)
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
