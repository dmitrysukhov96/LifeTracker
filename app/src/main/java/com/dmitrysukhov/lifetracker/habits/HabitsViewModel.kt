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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private fun calculateStreaks(events: List<HabitEvent>): Triple<Int, Int, List<Pair<Long, Long>>> {
        if (events.isEmpty()) return Triple(0, 0, emptyList())

        val sortedDates = events.map { it.date }.sorted()
        var currentStreak = 1
        var maxStreak = 1
        val streaks = mutableListOf<Pair<Long, Long>>()
        var currentStartDate = sortedDates.first()

        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] - sortedDates[i - 1] == 86400000L) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                streaks.add(Pair(currentStartDate, sortedDates[i - 1]))
                currentStreak = 1
                currentStartDate = sortedDates[i]
            }
        }
        streaks.add(Pair(currentStartDate, sortedDates.last()))
        return Triple(currentStreak, maxStreak, streaks)
    }

    fun getStreaksForHabit(habitId: Long): Flow<Triple<Int, Int, List<Pair<Long, Long>>>> =
        habitEventDao.getEventsForHabit(habitId)
            .map { events -> calculateStreaks(events) }

    /**
     * Возвращает метрики привычки в виде пары локализованных строк для отображения.
     * Для типа 0 (чекбокс) — текущий и максимальный стрик с датами.
     * Для типа 1/2 (числовые) — минимум и максимум с датами.
     */
    fun getHabitMetrics(habit: Habit, onResult: (Pair<String, String>) -> Unit) {
        viewModelScope.launch {
            habitEventDao.getEventsForHabit(habit.id).collect { events ->
                if (events.isEmpty()) {
                    onResult(Pair("Нет данных", "Нет данных"))
                    return@collect
                }
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                when (habit.type) {
                    0 -> { // Чекбокс — streak
                        val streaks = calculateStreaks(events)
                        val currentStreak = streaks.first
                        val maxStreak = streaks.second
                        val streakRanges = streaks.third
                        val currentRange = streakRanges.lastOrNull()
                        val maxRange = streakRanges.maxByOrNull { it.second - it.first }
                        val currentStr = if (currentRange != null) {
                            "$currentStreak дней подряд (${dateFormat.format(Date(currentRange.first))} – ${dateFormat.format(Date(currentRange.second))})"
                        } else "Нет данных"
                        val maxStr = if (maxRange != null) {
                            "Максимальный стрик: $maxStreak (${dateFormat.format(Date(maxRange.first))} – ${dateFormat.format(Date(maxRange.second))})"
                        } else "Нет данных"
                        onResult(Pair(currentStr, maxStr))
                    }
                    1, 2 -> { // Числовые
                        val minEvent = events.minByOrNull { it.value }
                        val maxEvent = events.maxByOrNull { it.value }
                        val minStr = if (minEvent != null) {
                            "Минимум: ${minEvent.value} (${dateFormat.format(Date(minEvent.date))})"
                        } else "Нет данных"
                        val maxStr = if (maxEvent != null) {
                            "Максимум: ${maxEvent.value} (${dateFormat.format(Date(maxEvent.date))})"
                        } else "Нет данных"
                        onResult(Pair(minStr, maxStr))
                    }
                    else -> onResult(Pair("Нет данных", "Нет данных"))
                }
            }
        }
    }
}

@Entity(tableName = "habit_events")
data class HabitEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val habitId: Long,
    val date: Long, val value: Float
)