package com.dmitrysukhov.lifetracker.daily

import androidx.lifecycle.ViewModel
import com.dmitrysukhov.lifetracker.TodoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DailyPlannerViewModel @Inject constructor() : ViewModel() {
    private val _tasks = MutableStateFlow<List<TodoItem>>(emptyList())
    val tasks: Flow<List<TodoItem>> = _tasks

//    fun setSelectedTasks(taskIds: List<Int>) {
//        // TODO: Сохранение выбранных задач
//    }
}