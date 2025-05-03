package com.dmitrysukhov.lifetracker.todo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.common.ui.ProjectTag
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val TODOLIST_SCREEN = "Todo List"

@Composable
fun TodoListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: TodoViewModel
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val todoList by viewModel.todoList.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    // Get localized category names
    val completedCategory = stringResource(R.string.completed_tasks)
    val noDateCategory = stringResource(R.string.no_date)
    val earlierCategory = stringResource(R.string.earlier)
    val yesterdayCategory = stringResource(R.string.yesterday)
    val todayCategory = stringResource(R.string.today)
    val tomorrowCategory = stringResource(R.string.tomorrow)
    val laterCategory = stringResource(R.string.later)

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
        val dayAfterTomorrowStart = tomorrowStart + (24 * 60 * 60 * 1000)
        todoList.forEach { task ->
            val category = when {
                task.isDone -> completedCategory
                task.dateTime == null -> noDateCategory
                task.dateTime < yesterdayStart -> earlierCategory
                task.dateTime < todayStart -> yesterdayCategory
                task.dateTime < tomorrowStart -> todayCategory
                task.dateTime < dayAfterTomorrowStart -> tomorrowCategory
                else -> laterCategory
            }

            if (!result.containsKey(category)) {
                result[category] = mutableListOf()
            }
            result[category]?.add(task)
        }

        // Define custom order for categories
        val categoryOrder = listOf(
            earlierCategory, yesterdayCategory, todayCategory, 
            noDateCategory, tomorrowCategory, laterCategory, completedCategory
        )

        // Return sorted map by custom order
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
                navController.navigate(NEW_TASK_SCREEN)
            }) { Icon(painterResource(R.drawable.plus), null, tint = Color.White) }
        })
    }

    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
    ) {
        Spacer(Modifier.height(16.dp))

        if (todoList.isEmpty()) EmptyPlaceholder(R.string.no_tasks, R.string.add_task_hint)
        else {
            LazyColumn(Modifier.padding(horizontal = 24.dp)) {
                for ((category, tasks) in categorizedTasks) {
                    // Category header
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
                                Text(
                                    text = category,
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
                                            if (expandedCategories[category]
                                                    ?: (category == todayCategory)
                                            ) 180f
                                            else 0f
                                        )
                                )
                            }
                        } else {
                            // Make Completed category collapsible too
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

                    // Show tasks if category expanded or it's "Today"
                    val isExpanded =
                        expandedCategories[category] ?: (category == todayCategory || category == noDateCategory)
                    if (isExpanded) {
                        items(tasks) { todoItem ->
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
                                isRunning = false,
                                onClick = {
                                    viewModel.selectedTask = todoItem
                                    navController.navigate(NEW_TASK_SCREEN)
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Spacer between categories
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(32.dp))
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
    item: TodoItem, 
    projects: List<Project>, 
    category: String,
    categoryNames: Map<String, String>,
    onCheckedChange: (Boolean) -> Unit,
    isRunning: Boolean, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(if (item.isDone) R.drawable.checked else R.drawable.not_checked),
            contentDescription = null, modifier = Modifier
                .clickable { onCheckedChange(!item.isDone) }
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.text,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                style = H2,
                color = if (item.isDone) PineColor else InverseColor,
            )
            item.durationMinutes?.let { duration -> DurationBadge(duration, isRunning) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            item.projectId?.let { projectId ->
                val project = projects.find { it.projectId == projectId }
                project?.let {
                    ProjectTag(text = it.title, color = Color(it.color))
                    Spacer(Modifier.height(8.dp))
                }
            }
            item.reminderTime?.let { time ->
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
                    
                    // Format the time string based on category
                    val timeString = formatTimeBasedOnCategory(time, category, categoryNames)
                    
                    // Determine text color based on task status and time
                    val now = System.currentTimeMillis()
                    val textColor = when {
                        item.isDone -> PineColor // Completed tasks always PineColor
                        time < now -> Color.Red // Overdue tasks in red
                        else -> PineColor // Future tasks in PineColor
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
fun formatTimeBasedOnCategory(time: Long, category: String, categoryNames: Map<String, String>): String {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    return when (category) {
        categoryNames["today"] -> timeFormatter.format(Date(time)) // Just time for today
        categoryNames["yesterday"] -> "Вчера в ${timeFormatter.format(Date(time))}" // Yesterday at HH:mm
        categoryNames["tomorrow"] -> "Завтра в ${timeFormatter.format(Date(time))}" // Tomorrow at HH:mm
        else -> dateTimeFormatter.format(Date(time)) // Full date and time for other categories
    }
}

fun formatDuration(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
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
fun DurationBadge(duration: Int, isRunning: Boolean) {
    val animatedDuration = remember { Animatable(0f) }
    LaunchedEffect(duration) {
        animatedDuration.animateTo(
            targetValue = duration.toFloat(),
            animationSpec = tween(durationMillis = 1000) // 1 секунда
        )
    }
    val backgroundColor = if (isRunning) PineColor else BgColor
    val contentColor = if (isRunning) Color.White else PineColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .height(18.dp)
            .clip(CircleShape)
            .border(1.dp, PineColor, CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 6.dp)
    ) {
        Icon(
            painter = painterResource(if (isRunning) R.drawable.stop else R.drawable.play),
            contentDescription = null, tint = contentColor, modifier = Modifier.size(8.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatDuration(animatedDuration.value.toInt()),
            color = contentColor, style = Small
        )
    }
}