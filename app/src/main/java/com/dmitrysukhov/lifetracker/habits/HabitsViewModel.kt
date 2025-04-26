package com.dmitrysukhov.lifetracker.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dmitrysukhov.lifetracker.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitDao: HabitDao, private val habitEventDao: HabitEventDao
) : ViewModel() {
    var selectedHabit: Habit? = null
    val habits: StateFlow<List<Habit>> = habitDao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(habit: Habit) {
        viewModelScope.launch { habitDao.insert(habit) }
    }

    fun saveHabitEvent(habitId: Long, dateMs: Long, value: Float) {
        viewModelScope.launch {
            val event = HabitEvent(habitId = habitId, date = dateMs, value = value)
            habitEventDao.insert(event)
        }
    }

    fun getEventsForHabit(habitId: Long): Flow<Map<Long, Float>> =
        habitEventDao.getEventsForHabit(habitId)
            .map { events -> events.associate { it.date to it.value } }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { habitDao.update(habit) }
    }

    fun deleteHabitEvent(habitId: Long, date: Long) {
        viewModelScope.launch { habitEventDao.deleteEvent(habitId, date) }
    }

    fun deleteHabit(id: Long) = viewModelScope.launch {
        habitEventDao.deleteAllEventsForHabit(id)
        habitDao.deleteHabit(id)
    }
}

@Entity(tableName = "habit_events")
data class HabitEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val habitId: Long,
    val date: Long, val value: Float
)