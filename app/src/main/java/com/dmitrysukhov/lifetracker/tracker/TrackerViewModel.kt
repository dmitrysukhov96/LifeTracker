package com.dmitrysukhov.lifetracker.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val eventDao: EventDao
) : ViewModel() {

    // Flow для получения последнего активного ивента
    private val _lastEvent = eventDao.getLastEventForProject(1).stateIn(
        viewModelScope, SharingStarted.Lazily, null // начальное значение
    )
    val lastEvent: StateFlow<Event?> = _lastEvent

    // Метод для начала события
    fun startEvent(projectId: Long) {
        val startTime = System.currentTimeMillis()
        val newEvent = Event(
            projectId = projectId,
            startTime = startTime,
            endTime = null
        )

        viewModelScope.launch {
            eventDao.insertEvent(newEvent)  // Вставляем новое событие в базу
        }
    }

    // Метод для остановки события
    fun stopEvent() {
        viewModelScope.launch {
            val currentEvent = _lastEvent.value
            if (currentEvent != null && currentEvent.endTime == null) {
                val endTime = System.currentTimeMillis()
                val updatedEvent = currentEvent.copy(endTime = endTime)
                eventDao.updateEvent(updatedEvent)  // Обновляем событие в базе
            }
        }
    }
}