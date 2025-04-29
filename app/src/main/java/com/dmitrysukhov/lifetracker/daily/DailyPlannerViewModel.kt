package com.dmitrysukhov.lifetracker.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

import com.dmitrysukhov.lifetracker.todo.TodoDao
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class DailyPlannerViewModel @Inject constructor() : ViewModel() {
    private val _tasks = MutableStateFlow<List<TodoItem>>(emptyList())
    val tasks: Flow<List<TodoItem>> = _tasks

//    fun updateTasks(newTasks: List<TodoItem>) {
//        _tasks.value = newTasks
//    }

    @Inject
    lateinit var todoDao: TodoDao
    @Inject
    lateinit var projectDao: ProjectDao

    init {
        viewModelScope.launch {
            launch {
                _tasks.value = todoDao.getAllTasks() // just assign the result, no collectLatest
            }
            launch {
                projectDao.getAllProjects().collectLatest { list ->
                    // Handle projects if needed
                }
            }
        }
    }
}