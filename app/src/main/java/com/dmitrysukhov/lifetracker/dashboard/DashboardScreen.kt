package com.dmitrysukhov.lifetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.daily.DAILY_PLANNER_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitsViewModel
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.isDarkTheme
import java.util.Calendar

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    todoViewModel: TodoViewModel, habitsViewModel: HabitsViewModel
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> stringResource(R.string.good_night)
        hour < 12 -> stringResource(R.string.good_morning)
        hour < 17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
    val context = LocalContext.current
    val tasks = todoViewModel.todoList.collectAsStateWithLifecycle(listOf()).value
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    
    val completedTasks = tasks.count { it.isDone }
    val totalTasks = tasks.size
    val completedHabits = habits.size // TODO: implement real habit completion tracking

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(context.getString(R.string.app_name)) {
                IconButton({ navController.navigate(DAILY_PLANNER_SCREEN) }) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        val sharedPref = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "") ?: ""

        Text(
            text = greeting + if (userName.isNotBlank()) ", $userName!" else "!",
            style = H1, color = InverseColor, modifier = Modifier.padding(start = 2.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Today's Overview Card
        StatCard(
            title = stringResource(R.string.todays_overview),
            iconPainter = painterResource(R.drawable.plus),
            content = {
                Column {
                    StatRowWithPainter(stringResource(R.string.tasks_completed_row), "$completedTasks/$totalTasks", painterResource(R.drawable.plus))
                    StatRowWithPainter(stringResource(R.string.habits_tracked), "$completedHabits/${habits.size}", painterResource(R.drawable.plus))
                    
                    // Get focus sessions count from shared preferences
                    val statsPref = context.getSharedPreferences("user_stats", android.content.Context.MODE_PRIVATE)
                    val focusSessionsCount = statsPref.getInt("focus_sessions_count", 0)
                    StatRowWithPainter(stringResource(R.string.focus_sessions), "$focusSessionsCount", painterResource(R.drawable.plus))
                    
                    StatRowWithPainter(stringResource(R.string.focus_time), "2h 30m", painterResource(R.drawable.plus))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Progress Card
        StatCard(
            title = stringResource(R.string.weekly_progress),
            iconPainter = painterResource(R.drawable.plus),
            content = {
                Column {
                    StatRowWithPainter(stringResource(R.string.tasks), "85%", painterResource(R.drawable.plus))
                    StatRowWithPainter(stringResource(R.string.habits), "92%", painterResource(R.drawable.plus))
                    StatRowWithPainter(stringResource(R.string.focus), "78%", painterResource(R.drawable.plus))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Top Habits Card
        StatCard(
            title = stringResource(R.string.top_habits),
            iconPainter = painterResource(R.drawable.plus),
            content = {
                Column {
                    habits.take(3).forEach { habit ->
                        HabitProgressRow(habit.title, 0.85f)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Project Status Card
        StatCard(
            title = stringResource(R.string.project_status),
            iconPainter = painterResource(R.drawable.plus),
            content = {
                Column {
                    ProjectStatusRow(stringResource(R.string.website_redesign), "75%", Color(0xFF4CAF50))
                    ProjectStatusRow(stringResource(R.string.mobile_app), "45%", Color(0xFF2196F3))
                    ProjectStatusRow(stringResource(R.string.marketing), "30%", Color(0xFFFFC107))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Productivity Insights Card
        StatCard(
            title = stringResource(R.string.productivity_insights),
            iconPainter = painterResource(R.drawable.plus),
            content = {
                Column {
                    InsightRow(stringResource(R.string.most_productive_day), stringResource(R.string.wednesday))
                    InsightRow(stringResource(R.string.best_time), "9:00 AM - 11:00 AM")
                    InsightRow(stringResource(R.string.focus_score), "8.5/10")
                }
            }
        )
        Spacer(Modifier.height(68.dp))
    }
}

@Composable
fun StatCard(title: String, iconPainter: Painter, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = (Color.White.copy(if (isDarkTheme()) 0.05f else 1f)))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = PineColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = H2.copy(fontWeight = FontWeight.Bold),
                    color = InverseColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun StatRowWithPainter(
    label: String,
    value: String,
    iconPainter: Painter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = PineColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label, 
                style = SimpleText,
                color = InverseColor
            )
        }
        Text(
            text = value,
            style = SimpleText.copy(fontWeight = FontWeight.Bold),
            color = PineColor
        )
    }
}

@Composable
fun HabitProgressRow(habitName: String, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = habitName, style = SimpleText, color = InverseColor)
            Text(
                text = "${(progress * 100).toInt()}%", color = PineColor,
                style = SimpleText.copy(fontWeight = FontWeight.Bold),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
        color = PineColor,
        trackColor = PineColor.copy(alpha = 0.2f))
    }
}

@Composable
fun ProjectStatusRow(projectName: String, progress: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = projectName, style = SimpleText, color = InverseColor)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = progress, style = SimpleText.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SimpleText, color = InverseColor)
        Text(text = value, style = SimpleText.copy(fontWeight = FontWeight.Bold), color = PineColor)
    }
}