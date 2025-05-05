package com.dmitrysukhov.lifetracker.todo

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.common.ui.TimeTracker
import com.dmitrysukhov.lifetracker.tracker.TrackerViewModel
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.projects.ProjectsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.dmitrysukhov.lifetracker.common.ui.ProjectTag as UIProjectTag

const val TODOLIST_SCREEN = "Todo List"

@Composable
fun TodoListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: TodoViewModel, trackerViewModel: TrackerViewModel = hiltViewModel(),
    projectsViewModel: ProjectsViewModel
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val todoList by viewModel.todoList.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }
    val lastEvent by trackerViewModel.lastEvent.collectAsStateWithLifecycle()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Refresh timer every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            refreshTrigger++
        }
    }

    // Get localized category names
    val completedCategory = stringResource(R.string.completed_tasks)
    val noDateCategory = stringResource(R.string.no_date)
    val earlierCategory = stringResource(R.string.earlier)
    val yesterdayCategory = stringResource(R.string.yesterday)
    val todayCategory = stringResource(R.string.today)
    val tomorrowCategory = stringResource(R.string.tomorrow)
    val laterCategory = stringResource(R.string.later)

    // Track active task based on event name
    val activeTask = remember(todoList, lastEvent) {
        if (lastEvent != null && lastEvent?.endTime == null) {
            todoList.find { it.text == lastEvent?.name && it.estimatedDurationMs != null && it.estimatedDurationMs > 0 }
        } else null
    }

    // Categorize items first
    val categorizedTasks = remember(
        todoList, completedCategory, noDateCategory,
        earlierCategory, yesterdayCategory, todayCategory, tomorrowCategory, laterCategory
    ) {
        val result = mutableMapOf<String, MutableList<TodoItem>>()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000)
        val tomorrowStart = todayStart + (24 * 60 * 60 * 1000)
        todoList.forEach { task ->
            val category = when {
                task.isDone -> completedCategory
                task.dateTime == null -> noDateCategory
                task.dateTime < yesterdayStart -> earlierCategory
                task.dateTime < todayStart -> yesterdayCategory
                task.dateTime < tomorrowStart -> todayCategory
                task.dateTime < tomorrowStart + (24 * 60 * 60 * 1000) -> tomorrowCategory
                else -> laterCategory
            }

            if (!result.containsKey(category)) {
                result[category] = mutableListOf()
            }
            result[category]?.add(task)
        }
        val categoryOrder = listOf(
            earlierCategory, yesterdayCategory, todayCategory, 
            noDateCategory, tomorrowCategory, laterCategory, completedCategory
        )
        LinkedHashMap<String, List<TodoItem>>().apply {
            categoryOrder.forEach { category ->
                result[category]?.let { tasks ->
                    if (tasks.isNotEmpty()) {
                        this[category] = tasks
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(context.getString(R.string.todo_list)) {
            IconButton(onClick = { showImportDialog = true }) {
                Icon(
                    painterResource(R.drawable.import_icon),
                    contentDescription = stringResource(R.string.import_tasks),
                    tint = Color.White
                )
            }
            IconButton({
                viewModel.selectedTask = null
                projectsViewModel.clearLastCreatedProjectId()
                navController.navigate(NEW_TASK_SCREEN)
            }) { Icon(painterResource(R.drawable.plus), null, tint = Color.White) }
        })
    }

    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
    ) {
        if (activeTask != null && lastEvent?.endTime == null) {
            TimeTracker(
                lastEvent = lastEvent, projects = projects,
                onActionClick = { viewModel.stopTracking() }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (todoList.isEmpty()) EmptyPlaceholder(R.string.no_tasks, R.string.add_task_hint) else {
            LazyColumn(Modifier.padding(24.dp)) {
                for ((category, tasks) in categorizedTasks) {
                    item {
                        if (category != completedCategory) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedCategories[category] =
                                            !(expandedCategories[category]
                                                ?: (category == todayCategory))
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category, style = H1, color = PineColor)
                                Icon(
                                    painter = painterResource(R.drawable.arrow_down),
                                    contentDescription = null, tint = PineColor, modifier = Modifier
                                        .size(16.dp)
                                        .rotate(
                                            if (expandedCategories[category]
                                                    ?: (category == todayCategory)
                                            ) 180f else 0f
                                        )
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedCategories[completedCategory] =
                                            expandedCategories[completedCategory] != true
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = completedCategory,
                                    style = H1,
                                    color = PineColor
                                )
                                Icon(
                                    painter = painterResource(R.drawable.arrow_down),
                                    contentDescription = null,
                                    tint = PineColor,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(
                                            if (expandedCategories[completedCategory] == true) 180f
                                            else 0f
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    val isExpanded =
                        expandedCategories[category] ?: (category == todayCategory || category == noDateCategory)
                    if (isExpanded) {
                        items(tasks) { todoItem ->
                            val isRunning =
                                todoItem.text == lastEvent?.name && lastEvent?.endTime == null
                            var remainingDuration: Long by remember(
                                todoItem.id,
                                todoItem.estimatedDurationMs
                            ) {
                                mutableLongStateOf(todoItem.estimatedDurationMs ?: 0)
                            }

                            TodoListItem(
                                item = todoItem,
                                projects = projects,
                                category = category,
                                categoryNames = mapOf(
                                    "today" to todayCategory,
                                    "yesterday" to yesterdayCategory,
                                    "tomorrow" to tomorrowCategory,
                                    "earlier" to earlierCategory,
                                    "later" to laterCategory,
                                    "completed" to completedCategory
                                ),
                                onCheckedChange = { isChecked ->
                                    viewModel.updateTask(
                                        todoItem.copy(isDone = isChecked)
                                    )
                                },
                                isRunning = isRunning,
                                onClick = {
                                    viewModel.selectedTask = todoItem
                                    navController.navigate(NEW_TASK_SCREEN)
                                },
                                onDurationClick = { currentRemainingMs ->
                                    if (todoItem.estimatedDurationMs != null && todoItem.estimatedDurationMs > 0) {
                                        if (isRunning) {
                                            val remainingMs = currentRemainingMs * 1000L
                                            val updatedTask =
                                                todoItem.copy(estimatedDurationMs = remainingMs)
                                            viewModel.stopTrackingWithTask(updatedTask)
                                        } else {
                                            viewModel.startTracking(todoItem)
                                        }
                                    }
                                },
                                onRemainingSecondsChanged = { seconds ->
                                    remainingDuration = seconds.toLong() * 1000L
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                item {
                    Spacer(modifier = Modifier.height(124.dp))
                }
            }
        }

        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text(text = stringResource(R.string.import_tasks), style = H1) },
                text = {
                    Column {
                        Text(text = stringResource(R.string.import_tasks_hint), style = SimpleText)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 10
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        importText.lines().forEach { line ->
                            if (line.isNotBlank()) {
                                viewModel.addTask(text = line.trim())
                            }
                        }
                        showImportDialog = false
                        importText = ""
                    }) {
                        Text(
                            text = stringResource(R.string.imp),
                            style = SimpleText.copy(fontWeight = Bold)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = SimpleText.copy(fontWeight = Bold)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TodoListItem(
    item: TodoItem, projects: List<Project>, category: String, categoryNames: Map<String, String>,
    onCheckedChange: (Boolean) -> Unit, isRunning: Boolean, onClick: () -> Unit,
    onDurationClick: (Int) -> Unit, onRemainingSecondsChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(if (item.isDone) R.drawable.checked else R.drawable.not_checked),
            contentDescription = null, modifier = Modifier
                .clickable { onCheckedChange(!item.isDone) }
                .size(20.dp)
        )
        Column(Modifier.padding(start = 28.dp, end = 120.dp)) {
            Text(
                text = item.text, maxLines = 2, overflow = TextOverflow.Ellipsis,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                style = SimpleText, color = if (item.isDone) PineColor else InverseColor
            )
            item.estimatedDurationMs?.let { duration ->
                Spacer(modifier = Modifier.height(8.dp))
                DurationBadge(
                    duration = duration,
                    isRunning = isRunning,
                    onClick = { remainingSeconds -> onDurationClick(remainingSeconds) },
                    onRemainingSecondsChanged = onRemainingSecondsChanged,
                    isEnabled = !item.isDone && (duration > 0 || isRunning)
                )
            }
        }
        Column(Modifier.align(Alignment.CenterEnd) , horizontalAlignment = Alignment.End) {
            Spacer(Modifier.height(2.dp))
            item.projectId?.let { projectId ->
                val project = projects.find { it.projectId == projectId }
                project?.let {
                    UIProjectTag(text = it.title, color = Color(project.color))
                    Spacer(Modifier.height(8.dp))
                }
            }
            item.dateTime?.let { time ->
                Row {
                    val startOfDay = getStartOfDay(time)
                    val animatedTime = remember { Animatable(startOfDay.toFloat()) }
                    LaunchedEffect(time) {
                        animatedTime.animateTo(
                            targetValue = time.toFloat(),
                            animationSpec = tween(durationMillis = 1000)
                        )
                    }
                    if (animatedTime.value >= time.toFloat() - 10) time
                    else animatedTime.value.toLong()

                    val timeString =
                        formatTimeBasedOnCategory(time, category, categoryNames, context)
                    val now = System.currentTimeMillis()
                    val textColor = when {
                        item.isDone -> PineColor
                        time < now -> Color.Red
                        else -> PineColor
                    }
                    
                    Text(
                        text = timeString, 
                        color = textColor,
                        style = Small
                    )
                    
                    Icon(
                        painter = painterResource(R.drawable.bell),
                        contentDescription = null, 
                        tint = textColor, 
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(14.dp)
                    )
                    item.repeatInterval?.let {
                        Icon(
                            painter = painterResource(R.drawable.repeat),
                            contentDescription = null,
                            tint = PineColor,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// Format time string based on category
fun formatTimeBasedOnCategory(
    time: Long,
    category: String,
    categoryNames: Map<String, String>,
    context: Context
): String {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    return when (category) {
        categoryNames["today"] -> timeFormatter.format(Date(time)) // Just time for today
        categoryNames["yesterday"] -> {
            context.getString(R.string.yesterday_at, timeFormatter.format(Date(time)))
        }

        categoryNames["tomorrow"] -> {
            context.getString(R.string.tomorrow_at, timeFormatter.format(Date(time)))
        }
        else -> dateTimeFormatter.format(Date(time)) // Full date and time for other categories
    }
}

fun getStartOfDay(currentTime: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@Composable
fun DurationBadge(
    duration: Long,
    isRunning: Boolean,
    onClick: (Int) -> Unit,
    onRemainingSecondsChanged: (Int) -> Unit,
    isEnabled: Boolean
) {
    val durationSeconds = (duration / 1000).toInt()
    var remainingSeconds by remember(
        durationSeconds,
        isRunning
    ) { mutableIntStateOf(durationSeconds) }
    var isCountingDown by remember(isRunning) { mutableStateOf(isRunning) }

    LaunchedEffect(remainingSeconds) {
        if (isCountingDown) {
            onRemainingSecondsChanged(remainingSeconds)
        }
    }

    LaunchedEffect(isRunning) {
        isCountingDown = isRunning
        if (!isRunning) {
            remainingSeconds = durationSeconds
        }
    }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            val startTimeMs = System.currentTimeMillis()

            while (isCountingDown) {
                val elapsedTimeMs = System.currentTimeMillis() - startTimeMs
                val elapsedSeconds = (elapsedTimeMs / 1000).toInt()
                val newRemainingSeconds = (durationSeconds - elapsedSeconds).coerceAtLeast(0)

                if (newRemainingSeconds != remainingSeconds) {
                    remainingSeconds = newRemainingSeconds
                }

                delay(1000)

                if (remainingSeconds <= 0) {
                    isCountingDown = false
                    break
                }
            }
        }
    }

    val effectivelyEnabled = isEnabled || isCountingDown

    val backgroundColor = when {
        !effectivelyEnabled -> Color.Gray
        isCountingDown -> PineColor
        else -> BgColor
    }

    val contentColor = when {
        !effectivelyEnabled -> Color.DarkGray
        isCountingDown -> Color.White
        else -> PineColor
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .height(18.dp)
            .clip(CircleShape)
            .border(1.dp, if (effectivelyEnabled) PineColor else Color.Gray, CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 6.dp)
            .clickable(enabled = effectivelyEnabled) {
                onClick(remainingSeconds)
                if (isCountingDown && !isRunning) {
                    isCountingDown = false
                }
            }
    ) {
        Icon(
            painter = painterResource(if (isCountingDown) R.drawable.stop else R.drawable.play),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(8.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        val timeText = formatDuration(if (isCountingDown) remainingSeconds else durationSeconds)
        Text(
            text = timeText,
            color = contentColor,
            style = Small
        )
    }
}

fun formatDuration(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
}