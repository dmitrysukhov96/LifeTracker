package com.dmitrysukhov.lifetracker.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.dashboard.TimeRange
import com.dmitrysukhov.lifetracker.projects.ProjectRepository
import com.dmitrysukhov.lifetracker.widgets.WidgetUpdater
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
    private val projectRepository: ProjectRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _lastEvent = MutableStateFlow<Event?>(null)
    val lastEvent: StateFlow<Event?> = _lastEvent

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _refreshTrigger = MutableStateFlow(0)

    private val _eventStats = MutableStateFlow<Map<Long?, Long>>(emptyMap())
    val eventStats: StateFlow<Map<Long?, Long>> = _eventStats

    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    init {
        viewModelScope.launch {
            eventRepository.getLastEvent().collect { event ->
                _lastEvent.value = event
            }
        }
        loadProjects()
        loadEventStats()
    }

    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        loadEventStats()
    }

    private fun loadEventStats() {
        viewModelScope.launch {
            val (startTime, endTime) = when (_selectedTimeRange.value) {
                TimeRange.ALL_TIME -> Pair(0L, System.currentTimeMillis())
                TimeRange.MONTH -> {
                    val now = LocalDate.now()
                    val startOfMonth = now.withDayOfMonth(1).toDateTimeAtStartOfDay().millis
                    val endOfMonth = now.plusMonths(1).withDayOfMonth(1).toDateTimeAtStartOfDay().minusMillis(1).millis
                    Pair(startOfMonth, endOfMonth)
                }
                TimeRange.WEEK -> {
                    val now = LocalDate.now()
                    val startOfWeek = now.withDayOfWeek(1).toDateTimeAtStartOfDay().millis
                    val endOfWeek = now.plusWeeks(1).withDayOfWeek(1).toDateTimeAtStartOfDay().minusMillis(1).millis
                    Pair(startOfWeek, endOfWeek)
                }
                TimeRange.DAY -> {
                    val now = LocalDate.now()
                    val startOfDay = now.toDateTimeAtStartOfDay().millis
                    val endOfDay = now.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1).millis
                    Pair(startOfDay, endOfDay)
                }
            }
            
            println("Loading events from $startTime to $endTime for ${_selectedTimeRange.value}")
            
            eventRepository.getEventsForTimeRange(startTime, endTime).collect { events ->
                println("Received ${events.size} events")
                events.forEach { event ->
                    println("Event: id=${event.eventId}, projectId=${event.projectId}, start=${event.startTime}, end=${event.endTime}")
                }
                
                val stats = events.groupBy { it.projectId }
                    .mapValues { (_, events) ->
                        events.sumOf { event ->
                            val endTime = event.endTime ?: System.currentTimeMillis()
                            endTime - event.startTime
                        }
                    }
                println("Calculated stats: $stats")
                _eventStats.value = stats
            }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _projects.value = projects
            }
        }
    }

    fun refreshEvents() {
        viewModelScope.launch {
            eventRepository.getLastEvent().collect { event ->
                _lastEvent.value = event
                return@collect
            }
            _refreshTrigger.value += 1
            loadEventStats()
        }
    }

    fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        val startOfDay = date.toDateTimeAtStartOfDay().millis
        val endOfDay = date.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1).millis
        return eventRepository.getEventsForTimeRange(startOfDay, endOfDay)
    }

    fun stopEvent() {
        viewModelScope.launch {
            _lastEvent.value?.let { event ->
                val updatedEvent = event.copy(endTime = System.currentTimeMillis())
                eventRepository.updateEvent(updatedEvent)
                _lastEvent.value = updatedEvent
                refreshEvents()
                
                // Update the widget
                getApplication<Application>().applicationContext?.let { context ->
                    WidgetUpdater.updateWidgets(context)
                }
            }
        }
    }

    fun insertEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.insertEvent(event)
            refreshEvents()
            
            // Update the widget
            getApplication<Application>().applicationContext?.let { context ->
                WidgetUpdater.updateWidgets(context)
            }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.updateEvent(event)
            if (_lastEvent.value?.eventId == event.eventId) {
                _lastEvent.value = event
            }
            refreshEvents()
            
            // Update the widget
            getApplication<Application>().applicationContext?.let { context ->
                WidgetUpdater.updateWidgets(context)
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
            if (_lastEvent.value?.eventId == eventId) {
                _lastEvent.value = null
            }
            refreshEvents()
            
            // Update the widget
            getApplication<Application>().applicationContext?.let { context ->
                WidgetUpdater.updateWidgets(context)
            }
        }
    }
}