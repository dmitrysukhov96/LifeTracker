package com.dmitrysukhov.lifetracker.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.dmitrysukhov.lifetracker.tracker.NewEventDialog
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
import java.util.concurrent.TimeUnit

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun CategoryBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .background(PineColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(text = title, style = H2.copy(fontWeight = Bold), color = InverseColor)
        Spacer(Modifier.height(8.dp))
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
    if (showTaskDialog) {
        NewEventDialog(
            projects = projects,
            onDismiss = { showTaskDialog = false },
            onSave = { event ->
                if (lastEvent?.endTime == null) {
                    trackerViewModel.stopEvent()
                }
                trackerViewModel.insertEvent(event)
                showTaskDialog = false
            },
            selectedProjectId = selectedProjectId,
            onNavigateToNewProject = { navController.navigate(NEW_PROJECT_SCREEN) },
            setProjectId = { selectedProjectId = it }
        )
    }
    Column(
        Modifier
            .background(BgColor)
            .fillMaxSize()
    ) {
        TimeTracker(
            lastEvent = lastEvent, 
            projects = projects,
            onActionClick = { 
                selectedEvent = null
                isTrackerStart = true
                showTaskDialog = true
            },
            onCircleButtonClick = { 
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    isTrackerStart = true
                    showTaskDialog = true
                } else {
                    trackerViewModel.stopEvent()
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            Text(text = greeting, style = H1, color = PineColor)
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val tomorrowStart = todayStart + 24 * 60 * 60 * 1000
            val todayTasks =
                tasks.filter { it.dateTime != null && it.dateTime >= todayStart && it.dateTime < tomorrowStart && !it.isDone }

            CategoryBlock(title = stringResource(R.string.todays_tasks)) {
                if (todayTasks.isEmpty()) Text(
                    text = stringResource(R.string.no_tasks_for_today), style = SimpleText,
                    color = PineColor
                )
                else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(Date(it.completeDate!!))
                }.mapValues { it.value.size }
            val recordDay = completedByDay.maxByOrNull { it.value }
            CategoryBlock(title = stringResource(R.string.tasks_stats)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tasks_completed_of_total, completedTasks, totalTasks), style = H2,
                    color = InverseColor
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.padding(vertical = 6.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.this_month), style = SimpleText, color = InverseColor)
                        Text(
                            text = "$monthCompleted", style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.this_week), style = SimpleText, color = InverseColor)
                        Text(
                            text = "$weekCompleted", style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.this_day), style = SimpleText, color = InverseColor)
                        Text(
                            text = "$todayCompleted", style = SimpleText.copy(fontWeight = Bold),
                            color = PineColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (recordDay != null) Text(
                    text = stringResource(R.string.record_tasks, recordDay.value, recordDay.key),
                    style = SimpleText.copy(fontWeight = Bold), color = PineColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            CategoryBlock(title = stringResource(R.string.projects_section)) { ProjectsSection(projects, tasks) }
            CategoryBlock(title = stringResource(R.string.habits_metrics)) { HabitsMetricsSection(habitsViewModel) }
            CategoryBlock(title = stringResource(R.string.tracker_stats)) { TrackerStatsSection(trackerViewModel) }
            CategoryBlock(title = stringResource(R.string.turbo_mode_sessions)) { TurboModeSection() }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun TrackerStatsSection(
    trackerViewModel: TrackerViewModel
) {
    val projects by trackerViewModel.projects.collectAsStateWithLifecycle()
    val eventStats by trackerViewModel.eventStats.collectAsStateWithLifecycle()
    val selectedTimeRange by trackerViewModel.selectedTimeRange.collectAsStateWithLifecycle()
    
    // Calculate project durations
    val projectDurations = projects.mapNotNull { project ->
        eventStats[project.projectId]?.let { duration ->
            project.projectId to duration
        }
    }
    val noProjectDuration = eventStats[null] ?: 0L
    
    // Total duration should be sum of all project durations and no-project duration
    val totalDuration = projectDurations.sumOf { it.second } + noProjectDuration
    val totalDurationFormatted = formatDuration(totalDuration)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        TimeRange.entries.forEach { timeRange ->
            Text(
                text = when (timeRange) {
                    TimeRange.ALL_TIME -> stringResource(R.string.all_time)
                    TimeRange.MONTH -> stringResource(R.string.month)
                    TimeRange.WEEK -> stringResource(R.string.week)
                    TimeRange.DAY -> stringResource(R.string.day)
                },
                style = H2,
                color = if (timeRange == selectedTimeRange) PineColor else InverseColor.copy(0.5f),
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clickable { trackerViewModel.setTimeRange(timeRange) }
            )
        }
    }
    
    // Total tracked time
    TrackerProgressRow(
        stringResource(R.string.all_tracked_time),
        totalDurationFormatted,
        1f,
        PineColor
    )
    
    // Project times
    projectDurations.forEach { (projectId, duration) ->
        val project = projects.find { it.projectId == projectId }
        if (project != null && duration > 0) {
            val progress = if (totalDuration > 0) duration.toFloat() / totalDuration else 0f
            TrackerProgressRow(
                project.title,
                formatDuration(duration),
                progress,
                Color(project.color)
            )
        }
    }
    
    // Time without project
    if (noProjectDuration > 0) {
        val progress = if (totalDuration > 0) noProjectDuration.toFloat() / totalDuration else 0f
        TrackerProgressRow(
            stringResource(R.string.without_project),
            formatDuration(noProjectDuration),
            progress,
            Color.Gray
        )
    }
}

enum class TimeRange {
    ALL_TIME, MONTH, WEEK, DAY
}

private fun formatDuration(durationMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
}

@Composable
fun TrackerProgressRow(label: String, value: String, progress: Float, color: Color) {
    Column(Modifier.padding(vertical = 6.dp)) {
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
            ) { Text(text = value, style = SimpleText.copy(fontWeight = Bold), color = color) }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress }, trackColor = color.copy(alpha = 0.2f), modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)), color = color
        )
    }
}

@Composable
fun ProjectsSection(projects: List<Project>, tasks: List<TodoItem>) {
    if (projects.isEmpty()) {
        Text(
            text = stringResource(R.string.no_projects),
            style = SimpleText,
            color = PineColor
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            projects.forEach { project ->
                val projectTasks = tasks.filter { it.projectId == project.projectId }
                val completedTasks = projectTasks.count { it.isDone }
                val totalTasks = projectTasks.size
                val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    val projectColor = Color(project.color)
                    Text(
                        text = project.title, style = SimpleText, color = projectColor, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = "$completedTasks/$totalTasks", overflow = TextOverflow.Ellipsis,
                        style = SimpleText.copy(fontWeight = Bold), color = projectColor, maxLines = 1,
                        modifier = Modifier
                            .width(50.dp)
                            .padding(start = 4.dp)
                    )
                    LinearProgressIndicator(
                        progress = { progress }, modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(1.dp)), color = projectColor,
                        trackColor = PineColor.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%", overflow = TextOverflow.Ellipsis,
                        style = SimpleText.copy(fontWeight = Bold), color = projectColor, maxLines = 1,
                        modifier = Modifier
                            .width(50.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitsMetricsSection(habitsViewModel: HabitsViewModel) {
    val habits = habitsViewModel.habits.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    
    if (habits.isEmpty()) {
        Text(
            text = stringResource(R.string.no_habits_yet),
            style = SimpleText,
            color = PineColor
        )
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(habits) { habit ->
                var metrics by remember { mutableStateOf(Pair("", "")) }
                LaunchedEffect(habit.id) {
                    habitsViewModel.getHabitMetrics(
                        habit = habit,
                        noDataString = context.getString(R.string.no_data),
                        daysInARowFormat = context.getString(R.string.days_in_a_row),
                        maxStreakFormat = context.getString(R.string.max_streak),
                        minimumFormat = context.getString(R.string.minimum),
                        maximumFormat = context.getString(R.string.maximum)
                    ) { metrics = it }
                }
                
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(habit.color).copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = Color(habit.color).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title
                        Text(
                            text = habit.title,
                            style = SimpleText.copy(
                                fontWeight = Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(habit.color)
                        )
                        
                        // Current metrics
                        Text(
                            text = metrics.first.split("(").first().trim(),
                            style = SimpleText.copy(
                                fontSize = 14.sp,
                                fontWeight = Medium
                            ),
                            color = InverseColor
                        )
                        
                        // Date range
                        Text(
                            text = metrics.first.split("(").getOrNull(1)?.removeSuffix(")") ?: "",
                            style = SimpleText.copy(
                                fontSize = 12.sp,
                                color = InverseColor.copy(alpha = 0.7f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Max metrics
                        Text(
                            text = metrics.second.split("(").first().trim(),
                            style = SimpleText.copy(
                                fontSize = 14.sp,
                                fontWeight = Medium
                            ),
                            color = InverseColor
                        )
                        
                        // Max date range
                        Text(
                            text = metrics.second.split("(").getOrNull(1)?.removeSuffix(")") ?: "",
                            style = SimpleText.copy(
                                fontSize = 12.sp,
                                color = InverseColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TurboModeSection() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("turbo_stats", Context.MODE_PRIVATE) }
    val sessions = remember { prefs.getInt("focus_sessions", 0) }
    val totalTimeMinutes = remember { prefs.getLong("total_focus_time", 0) / (1000 * 60) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(text = stringResource(R.string.sessions_count, sessions), style = SimpleText, color = PineColor)
        Text(text = stringResource(R.string.overall_time, totalTimeMinutes), style = SimpleText, color = PineColor)
    }
}