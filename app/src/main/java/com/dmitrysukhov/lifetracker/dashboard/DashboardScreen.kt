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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.TimeTracker
import com.dmitrysukhov.lifetracker.habits.HabitsViewModel
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.EventDialog
import com.dmitrysukhov.lifetracker.tracker.TrackerViewModel
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.isDarkTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    todoViewModel: TodoViewModel, habitsViewModel: HabitsViewModel, trackerViewModel: TrackerViewModel
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> stringResource(R.string.good_night)
        hour < 12 -> stringResource(R.string.good_morning)
        hour < 17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
        //todo add greeting
    val context = LocalContext.current
    val tasks = todoViewModel.todoList.collectAsStateWithLifecycle(listOf()).value
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    val projects = todoViewModel.projects.collectAsStateWithLifecycle().value
    val lastEvent = trackerViewModel.lastEvent.collectAsStateWithLifecycle().value
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var isTrackerStart by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    if (showTaskDialog) {
        EventDialog(
            event = selectedEvent,
            projects = projects,
            onDismiss = {
                showTaskDialog = false
                selectedEvent = null
            },
            onSave = { event ->
                if (event.eventId == 0L) trackerViewModel.insertEvent(event)
                else trackerViewModel.updateEvent(event)
                showTaskDialog = false
                selectedEvent = null
            },
            onDelete = { event ->
                trackerViewModel.deleteEvent(event.eventId)
                showTaskDialog = false
                selectedEvent = null
            },
            trackerStart = isTrackerStart,
            navController = navController
        )
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(BgColor)) {
        TimeTracker(
            lastEvent = lastEvent, projects = projects, onActionClick = {
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    isTrackerStart = true
                    showTaskDialog = true
                } else trackerViewModel.stopEvent()
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrowStart = todayStart + 24 * 60 * 60 * 1000
        val todayTasks = tasks.filter { it.dateTime != null && it.dateTime >= todayStart && it.dateTime < tomorrowStart }
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp), colors = cardColors(containerColor = PineColor.copy(0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.today) + " Tasks", style = H1)
                if (todayTasks.isEmpty()) {
                    Text(text = "No tasks for today", style = SimpleText)
                } else {
                    todayTasks.forEach { task ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = " - "+task.text, style = SimpleText, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Tasks Block
        val completedTasks = tasks.count { it.isDone }
        val totalTasks = tasks.size
        val now = System.currentTimeMillis()
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayCompleted = tasks.count { it.isDone && it.completeDate != null && it.completeDate >= todayStart && it.completeDate < tomorrowStart }
        val weekCompleted = tasks.count { it.isDone && it.completeDate != null && it.completeDate >= weekStart }
        val monthCompleted = tasks.count { it.isDone && it.completeDate != null && it.completeDate >= monthStart }
        // Record day
        val completedByDay = tasks.filter { it.isDone && it.completeDate != null }
            .groupBy { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it.completeDate!!)) }
            .mapValues { it.value.size }
        val recordDay = completedByDay.maxByOrNull { it.value }
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp), colors = cardColors(containerColor = PineColor.copy(0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Tasks", style = H1)
                Text(text = "$completedTasks/$totalTasks completed", style = SimpleText)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "This month completed: $monthCompleted", style = SimpleText)
                Text(text = "This week completed: $weekCompleted", style = SimpleText)
                Text(text = "Today completed: $todayCompleted", style = SimpleText)
                Spacer(modifier = Modifier.height(8.dp))
                if (recordDay != null) {
                    Text(text = "Record day: ${recordDay.key} - ${recordDay.value} tasks completed!", style = SimpleText, color = AccentColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
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
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = cardColors(containerColor = (Color.White.copy(if (isDarkTheme()) 0.05f else 1f)))
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