package com.dmitrysukhov.lifetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.daily.DAILY_PLANNER_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitsViewModel
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.util.Calendar

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    todoViewModel: TodoViewModel = hiltViewModel(),
    habitsViewModel: HabitsViewModel = hiltViewModel()
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> stringResource(R.string.good_night)
        hour < 12 -> stringResource(R.string.good_morning)
        hour < 17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(context.getString(R.string.app_name)) {
                IconButton({ navController.navigate(DAILY_PLANNER_SCREEN) }) {
                    Icon(Icons.Default.DateRange, null)
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(16.dp)
    ) {
        val sharedPref = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
val userName = sharedPref.getString("user_name", "") ?: ""

Text(text = greeting + if (userName.isNotBlank()) ", $userName!" else "!", style = H1)
        
        // Статистика задач
        val tasks = todoViewModel.todoList.collectAsState().value
        val completedTasks = tasks.count { it.isDone }
        val uncompletedTasks = tasks.size - completedTasks
        
        Text(
            text = "✅ Выполнено задач: $completedTasks",
            style = H2,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "⏳ Не выполнено: $uncompletedTasks",
            style = H2
        )

        // Статистика привычек
        // Обновленный блок статистики привычек
        val habits = habitsViewModel.habits.collectAsState().value
        LaunchedEffect(habits) {
            habits.forEach { habit ->
                habitsViewModel.getEventsForHabit(habit.id).collect { events ->
                    val values = events.values
                    val maxDaysStreak = calculateMaxStreak(events.keys)
                    val minValue = String.format("%.1f", values.minOrNull() ?: 0f)
                    val maxValue = String.format("%.1f", values.maxOrNull() ?: 0f)
                    val habitStats = """
                        ${habit.title}
                        🏆 Рекорд: ${maxDaysStreak}д 
                        📊 Min: ${minValue} / Max: ${maxValue}
                    """.trimIndent()
                    
                    // Добавьте компонент для отображения статистики привычки
                    // Например, Text(text = habitStats, ...)
                }
            }
        }
    }
}

private fun calculateMaxStreak(dates: Set<Long>): Int {
    val sortedDates = dates.sorted()
    var maxStreak = 0
    var currentStreak = 0
    var prevDate = 0L
    
    sortedDates.forEach { date ->
        if (prevDate == 0L || date - prevDate > 86400000) {
            currentStreak = 1
        } else {
            currentStreak++
        }
        if (currentStreak > maxStreak) maxStreak = currentStreak
        prevDate = date
    }
    return maxStreak
}