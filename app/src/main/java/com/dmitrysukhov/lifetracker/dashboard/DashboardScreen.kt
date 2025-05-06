package com.dmitrysukhov.lifetracker.dashboard

import android.content.Context
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.common.ui.TimeTracker
import com.dmitrysukhov.lifetracker.habits.HabitsViewModel
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsViewModel
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.EventDialog
import com.dmitrysukhov.lifetracker.tracker.TrackerViewModel
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun CategoryBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .background(
                PineColor.copy(alpha = 0.1f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = H2.copy(fontWeight = Bold),
            color = InverseColor,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    todoViewModel: TodoViewModel, habitsViewModel: HabitsViewModel,
    trackerViewModel: TrackerViewModel, projectsViewModel: ProjectsViewModel
) {
    val context = LocalContext.current
    val userName = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        .getString("user_name", "")
    setTopBarState(TopBarState(context.getString(R.string.app_name)))
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> stringResource(R.string.good_night)
        hour < 12 -> stringResource(R.string.good_morning)
        hour < 17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    } + if (!userName.isNullOrBlank()) ", $userName!" else "!"


    val tasks = todoViewModel.todoList.collectAsStateWithLifecycle(listOf()).value
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    val projects = projectsViewModel.projects
    val lastEvent = trackerViewModel.lastEvent.collectAsStateWithLifecycle().value
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var isTrackerStart by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    val lastProjectId by projectsViewModel.lastCreatedProjectId.collectAsState(null)
    var selectedProjectId by remember { mutableStateOf(selectedEvent?.projectId) }
    LaunchedEffect(lastProjectId) {
        lastProjectId?.let {
            showTaskDialog = true
            selectedProjectId = it
            projectsViewModel.clearLastCreatedProjectId()
        }
    }
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
        }, isTrackerStart, selectedProjectId, { navController.navigate(NEW_PROJECT_SCREEN) },
        { selectedProjectId = it }
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
            }, modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
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
                                    .padding(horizontal = 4.dp),
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$completedTasks of $totalTasks completed",
                    style = H2,
                    color = InverseColor
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.padding(vertical = 6.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(text = "This month", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$monthCompleted",
                            style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = "This week", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$weekCompleted",
                            style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = "Today", style = SimpleText, color = InverseColor)
                        Text(
                            text = "$todayCompleted",
                            style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (recordDay != null) {
                    Text(
                        text = "Record: ${recordDay.value} tasks on ${recordDay.key}",
                        style = SimpleText.copy(fontWeight = Bold),
                        color = PineColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            CategoryBlock(title = "📁 Projects") { ProjectsSection(projects, tasks) }
            CategoryBlock(title = "💪 Habits & Metrics") {
                if (habits.isEmpty()) {
                    Text(text = "No habits yet!", style = H2, color = PineColor)
                } else HabitsMetricsSection(habitsViewModel)

            }

            // Tracker Statistics
            CategoryBlock(title = "⏱️ Tracker Statistics") {
                TrackerStatsSection()
            }

            // Turbo Mode Sessions
            CategoryBlock(title = "🚀 Turbo Mode Sessions") {
                TurboModeSection()
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun TrackerStatsSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = "All time",
            color = PineColor,
            style = H2,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "Month", style = H2,
            color = InverseColor.copy(0.5f),
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = "Week", style = H2,
            color = InverseColor.copy(0.5f),
            modifier = Modifier.padding(end = 6.dp)
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
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = SimpleText, color = InverseColor)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = value,
                    style = SimpleText.copy(fontWeight = Bold),
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ProjectsSection(projects: List<Project>, tasks: List<TodoItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        projects.forEach { project ->
            val projectTasks = tasks.filter { it.projectId == project.projectId }
            val completedTasks = projectTasks.count { it.isDone }
            val totalTasks = projectTasks.size
            val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val projectColor = Color(project.color)
                Text(
                    text = project.title,
                    style = SimpleText,
                    color = projectColor, maxLines = 1,overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = "$completedTasks/$totalTasks",
                    style = SimpleText.copy(fontWeight = Bold),
                    color = projectColor, maxLines = 1,overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(50.dp).padding(start = 4.dp)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = projectColor,
                    trackColor = PineColor.copy(alpha = 0.2f),
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = SimpleText.copy(fontWeight = Bold),
                    color = projectColor,maxLines = 1,overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(50.dp).padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun HabitsMetricsSection(habitsViewModel: HabitsViewModel) {
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        habits.forEach { habit ->
            var metrics by remember { mutableStateOf(Pair("", "")) }
            LaunchedEffect(habit.id) {
                habitsViewModel.getHabitMetrics(habit) {
                    metrics = it
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.title,
                    style = SimpleText.copy(fontWeight = Bold),
                    color = Color(habit.color),
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.weight(2f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(metrics.first, style = SimpleText.copy(color = InverseColor))
                    Text(metrics.second, style = SimpleText.copy(color = InverseColor))
                }
            }
        }
    }
}

@Composable
fun TurboModeSection() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = "4 sessions",
            style = SimpleText,
            color = PineColor
        )
        Text(
            text = "40 min overall. Well done!",
            style = SimpleText,
            color = PineColor
        )
    }
}