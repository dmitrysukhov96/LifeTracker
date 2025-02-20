package com.dmitrysukhov.lifetracker.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dmitrysukhov.lifetracker.TodoDao

class TodoViewModelFactory(private val todoDao: TodoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(todoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
