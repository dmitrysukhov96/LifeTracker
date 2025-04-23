package com.dmitrysukhov.lifetracker.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.projects.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _lastEvent = MutableStateFlow<Event?>(null)
    val lastEvent: StateFlow<Event?> = _lastEvent

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    init {
        viewModelScope.launch {
            eventRepository.getLastEvent().collect { event ->
                _lastEvent.value = event
            }
        }
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _projects.value = projects
            }
        }
    }

    fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        val startOfDay = date.toDateTimeAtStartOfDay().millis
        val endOfDay = date.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1).millis
        return eventRepository.getEventsForTimeRange(startOfDay, endOfDay)
    }

    fun startEvent(projectId: Long?, taskName: String) {
        viewModelScope.launch {
            val event = Event(
                projectId = projectId,
                name = taskName,
                startTime = System.currentTimeMillis(),
                endTime = null
            )
            eventRepository.insertEvent(event)
            _lastEvent.value = event
        }
    }

    fun stopEvent() {
        viewModelScope.launch {
            _lastEvent.value?.let { event ->
                val updatedEvent = event.copy(endTime = System.currentTimeMillis())
                eventRepository.updateEvent(updatedEvent)
                _lastEvent.value = updatedEvent
            }
        }
    }

    fun insertEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.insertEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event)
            if (_lastEvent.value?.eventId == event.eventId) {
                _lastEvent.value = event
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
            if (_lastEvent.value?.eventId == eventId) {
                _lastEvent.value = null
            }
        }
    }
}