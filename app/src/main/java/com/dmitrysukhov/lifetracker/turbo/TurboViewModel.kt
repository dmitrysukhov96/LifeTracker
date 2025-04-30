package com.dmitrysukhov.lifetracker.turbo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.todo.TodoDao
import com.dmitrysukhov.lifetracker.tracker.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class TimerMode { STOPWATCH, COUNTDOWN }

enum class TimerState { IDLE, RUNNING, PAUSED, COMPLETED }

data class TurboSession(
    val selectedTask: TodoItem? = null,
    val timerMode: TimerMode = TimerMode.COUNTDOWN,
    val durationMinutes: Int = 45,
    val currentTimeMillis: Long = 0L,
    val timerState: TimerState = TimerState.IDLE,
    val startTimeMillis: Long? = null,
    val pauseTimeMillis: Long? = null,
    val event: Event? = null
)

@HiltViewModel
class TurboViewModel @Inject constructor(
    private val todoDao: TodoDao, private val eventRepository: EventRepository
) : ViewModel() {

    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> = _todoList.asStateFlow()

    private val _turboSession = MutableStateFlow(TurboSession())
    val turboSession: StateFlow<TurboSession> = _turboSession.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasks = todoDao.getAllTasks()
            _todoList.value = tasks.filter { !it.isDone }
        }
    }

    fun selectTask(task: TodoItem?) {
        _turboSession.value = _turboSession.value.copy(selectedTask = task)
    }

    fun setTimerMode(mode: TimerMode) {
        _turboSession.value = _turboSession.value.copy(timerMode = mode)
    }

    fun setDuration(minutes: Int) {
        _turboSession.value = _turboSession.value.copy(
            durationMinutes = minutes,
            currentTimeMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())
        )
    }

    fun startTimer() {
        val currentSession = _turboSession.value
        val now = System.currentTimeMillis()
        
        // Create event in tracker
        viewModelScope.launch {
            val task = currentSession.selectedTask
            val newEvent = Event(
                name = task?.text ?: "Focus Session",
                projectId = task?.projectId,
                startTime = now,
                endTime = null
            )
            
            // Insert the event and get the ID
            val eventId = eventRepository.insertEvent(newEvent)
            // Create a copy of the event with the assigned ID
            val savedEvent = newEvent.copy(eventId = eventId)
            
            val startTimeMillis = if (currentSession.timerMode == TimerMode.COUNTDOWN) {
                TimeUnit.MINUTES.toMillis(currentSession.durationMinutes.toLong())
            } else 0L
            
            _turboSession.value = currentSession.copy(
                timerState = TimerState.RUNNING,
                startTimeMillis = now,
                currentTimeMillis = startTimeMillis,
                event = savedEvent
            )
            
            startTimerJob()
        }
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_turboSession.value.timerState == TimerState.RUNNING) {
                delay(100) // Update every 100ms for smooth UI
                updateTimerValue()
            }
        }
    }

    private fun updateTimerValue() {
        val currentSession = _turboSession.value
        if (currentSession.timerState != TimerState.RUNNING) return

        val startTime = currentSession.startTimeMillis ?: return
        val now = System.currentTimeMillis()
        val elapsedTime = now - startTime

        when (currentSession.timerMode) {
            TimerMode.STOPWATCH -> {
                _turboSession.value = currentSession.copy(currentTimeMillis = elapsedTime)
            }
            TimerMode.COUNTDOWN -> {
                val initialDuration = TimeUnit.MINUTES.toMillis(currentSession.durationMinutes.toLong())
                val remainingTime = initialDuration - elapsedTime
                
                if (remainingTime <= 0) {
                    completeSession()
                } else {
                    _turboSession.value = currentSession.copy(currentTimeMillis = remainingTime)
                }
            }
        }
    }

    fun addTask(text: String) {
        val todoDao = this::class.java.getDeclaredField("todoDao").apply { isAccessible = true }.get(this) as TodoDao

        val newTask = TodoItem(
            text = text,
            description = "",
            dateTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1), // Tomorrow
            isDone = false
        )

        viewModelScope.launch {
            todoDao.insertTask(newTask)
            loadTasks()
        }
    }

    fun pauseTimer() {
        val currentSession = _turboSession.value
        if (currentSession.timerState == TimerState.RUNNING) {
            timerJob?.cancel()
            _turboSession.value = currentSession.copy(
                timerState = TimerState.PAUSED,
                pauseTimeMillis = System.currentTimeMillis()
            )
        }
    }

    fun resumeTimer() {
        val currentSession = _turboSession.value
        if (currentSession.timerState == TimerState.PAUSED) {
            val pausedAt = currentSession.pauseTimeMillis ?: return
            val now = System.currentTimeMillis()
            val pauseDuration = now - pausedAt
            
            // Adjust the start time to account for the pause duration
            val adjustedStartTime = (currentSession.startTimeMillis ?: 0) + pauseDuration
            
            _turboSession.value = currentSession.copy(
                timerState = TimerState.RUNNING,
                startTimeMillis = adjustedStartTime,
                pauseTimeMillis = null
            )
            
            startTimerJob()
        }
    }

    fun stopTimer() {
        completeSession()
    }

    private fun completeSession() {
        val currentSession = _turboSession.value
        timerJob?.cancel()
        
        viewModelScope.launch {
            // Update the event in tracker with end time
            currentSession.event?.let { event ->
                val updatedEvent = event.copy(endTime = System.currentTimeMillis())
                eventRepository.updateEvent(updatedEvent)
                
                // If there was a task selected, mark it as done
                currentSession.selectedTask?.let { task ->
                    val updatedTask = task.copy(isDone = true)
                    todoDao.updateTask(updatedTask)
                    loadTasks() // Refresh task list
                }
            }
            
            _turboSession.value = currentSession.copy(
                timerState = TimerState.COMPLETED
            )
        }
    }

//    fun resetSession() {
//        _turboSession.value = TurboSession()
//    }
} 