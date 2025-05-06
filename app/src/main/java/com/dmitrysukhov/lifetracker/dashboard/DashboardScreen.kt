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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun CategoryBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(
                if (isDarkTheme()) PineColor.copy(alpha = 0.1f) else Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = H2.copy(fontWeight = FontWeight.Bold),
            color = InverseColor,
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    todoViewModel: TodoViewModel, habitsViewModel: HabitsViewModel,
    trackerViewModel: TrackerViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { setTopBarState(TopBarState(context.getString(R.string.app_name))) }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> "🌙 Good night"
        hour < 12 -> "☀️ Good morning"
        hour < 17 -> "🌤️ Good afternoon"
        else -> "🌆 Good evening"
    }
    val tasks = todoViewModel.todoList.collectAsStateWithLifecycle(listOf()).value
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    val projects = todoViewModel.projects.collectAsStateWithLifecycle().value
    val lastEvent = trackerViewModel.lastEvent.collectAsStateWithLifecycle().value
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var isTrackerStart by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }

    if (showTaskDialog) EventDialog(
        event = selectedEvent, projects = projects, onDismiss = {
            showTaskDialog = false
            selectedEvent = null
        }, onSave = { event ->
            if (event.eventId == 0L) trackerViewModel.insertEvent(event)
            else trackerViewModel.updateEvent(event)
            showTaskDialog = false
            selectedEvent = null
        }, onDelete = { event ->
            trackerViewModel.deleteEvent(event.eventId)
            showTaskDialog = false
            selectedEvent = null
        }, trackerStart = isTrackerStart, navController = navController
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        TimeTracker(
            lastEvent = lastEvent, projects = projects, onActionClick = {
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    isTrackerStart = true
                    showTaskDialog = true
                } else trackerViewModel.stopEvent()
            }, modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text(text = greeting, style = H1, color = PineColor)

            // Today's Tasks
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val tomorrowStart = todayStart + 24 * 60 * 60 * 1000
            val todayTasks =
                tasks.filter { it.dateTime != null && it.dateTime >= todayStart && it.dateTime < tomorrowStart && !it.isDone }

            CategoryBlock(title = "📝 Today's Tasks") {
                if (todayTasks.isEmpty()) {
                    Text(
                        text = "No tasks for today! Enjoy your free time!",
                        style = SimpleText,
                        color = PineColor
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayTasks.forEach { task ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PineColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = task.text, style = SimpleText, color = InverseColor)
                            }
                        }
                    }
                }
            }

            // Tasks Statistics
            val completedTasks = tasks.count { it.isDone }
            val totalTasks = tasks.size
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
            val todayCompleted =
                tasks.count { it.isDone && it.completeDate != null && it.completeDate >= todayStart && it.completeDate < tomorrowStart }
            val weekCompleted =
                tasks.count { it.isDone && it.completeDate != null && it.completeDate >= weekStart }
            val monthCompleted =
                tasks.count { it.isDone && it.completeDate != null && it.completeDate >= monthStart }
            val completedByDay = tasks.filter { it.isDone && it.completeDate != null }
                .groupBy {
                    SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                    ).format(Date(it.completeDate!!))
                }
                .mapValues { it.value.size }
            val recordDay = completedByDay.maxByOrNull { it.value }

            CategoryBlock(title = "📊 Tasks Stats") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completedTasks",
                        style = H2.copy(fontWeight = FontWeight.Bold),
                        color = PineColor
                    )
                    Text(text = " of ", style = SimpleText, color = InverseColor)
                    Text(
                        text = "$totalTasks",
                        style = H2.copy(fontWeight = FontWeight.Bold),
                        color = PineColor
                    )
                    Text(text = " completed", style = SimpleText, color = InverseColor)
                }

                Row(Modifier.padding(vertical = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "This month", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$monthCompleted",
                            style = SimpleText.copy(fontWeight = FontWeight.Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = "This week", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$weekCompleted",
                            style = SimpleText.copy(fontWeight = FontWeight.Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = "Today", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$todayCompleted",
                            style = SimpleText.copy(fontWeight = FontWeight.Bold),
                            color = PineColor
                        )
                    }
                }

                if (recordDay != null) {
                    Text(
                        text = "Record: ${recordDay.value} tasks on ${recordDay.key}",
                        style = SimpleText,
                        color = AccentColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Projects
            CategoryBlock(title = "📁 Projects") {
                ProjectsSection(projects = projects)
            }

            // Habits & Metrics
            CategoryBlock(title = "💪 Habits & Metrics") {
                if (habits.isEmpty()) {
                    Text(text = "No habits yet!", style = SimpleText, color = PineColor)
                } else {
                    Column {
                        habits.forEach { habit ->
                            HabitProgressRow(
                                habitName = habit.title,
                                progress = 0.7f
                            ) // Mock progress
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HabitsMetricsSection(habits = habits)
            }

            // Tracker Statistics
            CategoryBlock(title = "⏱️ Tracker Statistics") {
                TrackerStatsSection()
            }

            // Turbo Mode Sessions
            CategoryBlock(title = "🚀 Turbo Mode Sessions") {
                TurboModeSection()
            }

            Spacer(Modifier.height(68.dp))
        }
    }
}

// --- MOCK/HELPER SECTIONS ---

@Composable
fun TrackerStatsSection() {
    // Mock toggles and progress bars
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = "All time",
            color = PineColor,
            style = H2,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "Month", style = H2,
            color = InverseColor.copy(0.5f),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "Week", style = H2,
            color = InverseColor.copy(0.5f),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = "Day", style = H2, color = InverseColor.copy(0.5f))
    }
    TrackerProgressRow("All Tracked Time", "38:55", 1f, PineColor)
    TrackerProgressRow("Church", "04:45", 0.12f, Color(0xFF4CAF50))
    TrackerProgressRow("Personal", "06:05", 0.16f, Color(0xFF2196F3))
    TrackerProgressRow("Work", "08:45", 0.23f, Color(0xFFFFC107))
    TrackerProgressRow("Without project", "25:44", 0.66f, Color.Gray)
}

@Composable
fun TrackerProgressRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, style = SimpleText, color = InverseColor)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = value,
                    style = SimpleText.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ProjectsSection(projects: List<com.dmitrysukhov.lifetracker.Project>) {
    // Mock data for demonstration
    val mockProjects = if (projects.isEmpty()) listOf(
        Triple("Website Redesign", 12 to 20, 0.6f),
        Triple("Mobile App", 9 to 15, 0.45f),
        Triple("Marketing", 3 to 10, 0.3f)
    ) else projects.map { Triple(it.title, 3 to 5, 0.6f) } // Replace with real stats
    Column {
        mockProjects.forEach { (name, pair, percent) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = SimpleText,
                    color = InverseColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${pair.first}/${pair.second}",
                    style = SimpleText.copy(fontWeight = FontWeight.Bold),
                    color = PineColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(4.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PineColor,
                    trackColor = PineColor.copy(alpha = 0.2f)
                )
                Text(
                    text = "${(percent * 100).toInt()}%",
                    style = SimpleText.copy(fontWeight = FontWeight.Bold),
                    color = PineColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HabitsMetricsSection(habits: List<com.dmitrysukhov.lifetracker.Habit>) {
    // Mock data for demonstration
    val mockHabits = if (habits.isEmpty()) listOf(
        Pair("Read", "3 days in a row! 28 May - 30 May\nMax streak: 5 days! 20 May - 25 May"),
        Pair("Weight", "min: 79.5, max: 82.5")
    ) else habits.map { it.title to "3 days in a row! 28 May - 30 May\nMax streak: 5 days! 20 May - 25 May" }
    Column {
        mockHabits.forEach { (name, desc) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = SimpleText.copy(fontWeight = FontWeight.Bold),
                    color = PineColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = desc,
                    style = SimpleText,
                    color = InverseColor,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
fun TurboModeSection() {
    // Mock data
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Turbo Mode: 4 sessions. 40 min overall. Well done!",
            style = SimpleText,
            color = PineColor
        )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors(
            containerColor = if (isDarkTheme()) PineColor.copy(alpha = 0.05f) else Color.White.copy(
                alpha = 0.9f
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PineColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = habitName, style = SimpleText, color = InverseColor)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PineColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = PineColor,
                        style = SimpleText.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PineColor,
                trackColor = PineColor.copy(alpha = 0.2f)
            )
        }
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