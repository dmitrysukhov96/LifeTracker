package com.dmitrysukhov.lifetracker.turbo

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.NewEventDialog
import com.dmitrysukhov.lifetracker.tracker.TrackerViewModel
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

const val TURBO_SCREEN = "turbo_screen"

enum class TimerMode { STOPWATCH, COUNTDOWN }

@Composable
fun TurboScreen(
    setTopBarState: (TopBarState) -> Unit, viewModel: TrackerViewModel,
    todoViewModel: TodoViewModel, navController: NavHostController
) {
    val lastEvent by viewModel.lastEvent.collectAsState()
    val tasks by todoViewModel.todoList.collectAsState()
    val context = LocalContext.current
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(TimerMode.COUNTDOWN) }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var selectedTask by remember { mutableStateOf<TodoItem?>(null) }
    var additionalEvent by remember { mutableStateOf<Event?>(null) }
    var showCompletionScreen by remember { mutableStateOf(false) }
    var completedEvent by remember { mutableStateOf<Event?>(null) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var cameFromSetup by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(title = context.getString(R.string.turbo_mode),
            screen = TURBO_SCREEN, color = PineColor))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(PineColor, AccentColor)))
    ) {
        when {
            showCompletionScreen -> {
                TurboCompletionScreen(
                    event = completedEvent,
                    onContinue = {
                        showCompletionScreen = false
                        selectedTask = null
                        additionalEvent = null
                        cameFromSetup = false
                    }
                )
            }
            // If there's an active event from tracker
            lastEvent != null && lastEvent?.endTime == null -> {
                TurboActiveScreen(
                    event = lastEvent,
                    selectedMode = selectedMode,
                    durationMinutes = durationMinutes,
                    cameFromSetup = cameFromSetup,
                    onStop = { 
                        val currentTime = System.currentTimeMillis()
                        val eventDuration = currentTime - (lastEvent?.startTime ?: currentTime)
                        viewModel.stopEvent()
                        completedEvent = lastEvent
                        showCompletionScreen = true
                        
                        // Update statistics
                        val prefs = context.getSharedPreferences("turbo_stats", Context.MODE_PRIVATE)
                        val sessions = prefs.getInt("focus_sessions", 0)
                        val totalTime = prefs.getLong("total_focus_time", 0)
                        
                        prefs.edit().apply {
                            putInt("focus_sessions", sessions + 1)
                            putLong("total_focus_time", totalTime + eventDuration)
                            apply()
                        }
                        
                        // Update task's estimated time if exists
                        selectedTask?.let { task ->
                            task.estimatedDurationMs?.let { estimatedTime ->
                                if (estimatedTime > 0) {
                                    val newEstimatedTime = maxOf(0, estimatedTime - eventDuration)
                                    todoViewModel.updateTask(
                                        task.copy(
                                            estimatedDurationMs = newEstimatedTime
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }
            // Otherwise show setup screen
            else -> {
                TurboSetupScreen(
                    tasks = tasks,
                    additionalEvent = additionalEvent,
                    selectedTask = selectedTask,
                    selectedMode = selectedMode,
                    durationMinutes = durationMinutes,
                    onTaskSelect = { selectedTask = it },
                    onModeSelect = { selectedMode = it },
                    onDurationChange = { durationMinutes = it },
                    onStart = { 
                        cameFromSetup = true
                        viewModel.insertEvent(
                            Event(
                                name = selectedTask?.text ?: context.getString(R.string.focus_session),
                                projectId = selectedTask?.projectId,
                                startTime = System.currentTimeMillis(),
                                endTime = null
                            )
                        )
                    },
                    onAddNewTask = {
                        showTaskDialog = true
                    }
                )
            }
        }

        if (showTaskDialog) {
            NewEventDialog(
                projects = viewModel.projects.collectAsState().value,
                onDismiss = { showTaskDialog = false },
                onSave = { event ->
                    additionalEvent = event
                    selectedTask = TodoItem(
                        text = event.name ?: "",
                        projectId = event.projectId,
                        dateTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                        isDone = false
                    )
                    showTaskDialog = false
                },
                selectedProjectId = selectedProjectId,
                onNavigateToNewProject = { 
                    navController.navigate(NEW_PROJECT_SCREEN)
                },
                setProjectId = { projectId ->
                    selectedProjectId = projectId
                }
            )
        }
    }
}

@Composable
fun TurboSetupScreen(
    tasks: List<TodoItem>, additionalEvent: Event?, selectedTask: TodoItem?,
    selectedMode: TimerMode, durationMinutes: Int, onTaskSelect: (TodoItem?) -> Unit,
    onModeSelect: (TimerMode) -> Unit, onDurationChange: (Int) -> Unit, onStart: () -> Unit,
    onAddNewTask: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf((durationMinutes / 60).toString()) }
    var minutes by remember { mutableStateOf((durationMinutes % 60).toString()) }
    
    LaunchedEffect(durationMinutes) {
        hours = (durationMinutes / 60).toString()
        minutes = (durationMinutes % 60).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.select_task),
            style = H2,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDropdown = true },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTask?.text ?: (stringResource(R.string.select_task) + "..."),
                        style = SimpleText,
                        color = Color.White
                    )
                    Icon(
                        painter = painterResource(R.drawable.arrow_down),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (showDropdown) 180f else 0f)
                    )
                }
            }

            DropdownMenu(
                expanded = showDropdown, onDismissRequest = {
                    showDropdown = false
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(BgColor)
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.add_task),
                            style = H2.copy(color = PineColor, fontWeight = Bold)
                        )
                    },
                    onClick = {
                        onAddNewTask()
                        showDropdown = false
                    }
                )

                additionalEvent?.let { event ->
                    DropdownMenuItem(
                        text = { Text(event.name ?: "", style = H2.copy(color = InverseColor)) },
                        onClick = {
                            onTaskSelect(TodoItem(
                                text = event.name ?: "",
                                projectId = event.projectId,
                                dateTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                                isDone = false
                            ))
                            showDropdown = false
                        }
                    )
                }

                tasks.forEach { task ->
                    DropdownMenuItem(
                        text = { Text(task.text, style = H2.copy(color = InverseColor)) },
                        onClick = {
                            onTaskSelect(task)
                            showDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.timer_settings) + ":",
                style = SimpleText, color = Color.White, fontWeight = Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedMode == TimerMode.STOPWATCH,
                onClick = { onModeSelect(TimerMode.STOPWATCH) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.7f)
                )
            )
            Text(
                text = stringResource(R.string.stopwatch), style = SimpleText, color = Color.White,
                modifier = Modifier.clickable { onModeSelect(TimerMode.STOPWATCH) }
            )

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = selectedMode == TimerMode.COUNTDOWN,
                onClick = { onModeSelect(TimerMode.COUNTDOWN) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = Color.White.copy(alpha = 0.7f)
                )
            )
            Text(
                text = stringResource(R.string.countdown), style = SimpleText, color = Color.White,
                modifier = Modifier.clickable { onModeSelect(TimerMode.COUNTDOWN) }
            )
        }

        if (selectedMode == TimerMode.COUNTDOWN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hours
                OutlinedTextField(
                    value = hours,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull() ?: 0 <= 23)) {
                            hours = newValue
                            val totalMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
                            onDurationChange(totalMinutes)
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    textStyle = H1.copy(color = Color.White, textAlign = TextAlign.Center),
                    label = {
                        Text(
                            stringResource(R.string.hours),
                            style = Small.copy(color = Color.White)
                        )
                    },
                    colors = colors(
                        unfocusedBorderColor = Color.White, focusedBorderColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Text(
                    text = ":", style = H1,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Minutes
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull() ?: 0 <= 59)) {
                            minutes = newValue
                            val totalMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
                            onDurationChange(totalMinutes)
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    textStyle = H1.copy(color = Color.White, textAlign = TextAlign.Center),
                    label = {
                        Text(
                            stringResource(R.string.minutes),
                            style = Small.copy(color = Color.White)
                        )
                    },
                    colors = colors(
                        unfocusedBorderColor = Color.White, focusedBorderColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        TurboButton(
            onClick = onStart,
            enabled = (selectedTask != null || additionalEvent != null) && 
                     (selectedMode == TimerMode.STOPWATCH || durationMinutes > 0)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun TurboActiveScreen(
    event: Event?,
    selectedMode: TimerMode,
    durationMinutes: Int,
    cameFromSetup: Boolean,
    onStop: () -> Unit
) {
    var timeElapsed by remember { mutableLongStateOf(0L) }
    var timeRemaining by remember { mutableLongStateOf(durationMinutes * 60L) }
    var isWarning by remember { mutableStateOf(false) }
    
    // Use stopwatch mode if we didn't come from setup screen (i.e. there was an active event)
    val effectiveMode = if (!cameFromSetup) TimerMode.STOPWATCH else selectedMode
    
    LaunchedEffect(event) {
        while (true) {
            if (event?.endTime == null) {
                timeElapsed = (System.currentTimeMillis() - (event?.startTime ?: 0)) / 1000
                if (effectiveMode == TimerMode.COUNTDOWN) {
                    timeRemaining = maxOf(0, durationMinutes * 60L - timeElapsed)
                    isWarning = timeRemaining <= 10
                    
                    if (timeRemaining == 0L) {
                        onStop()
                        break
                    }
                }
            }
            delay(1000)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = event?.name ?: stringResource(R.string.focus_session),
            style = H1, 
            color = Color.White, 
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp)
        )
        
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (effectiveMode == TimerMode.COUNTDOWN) {
                    formatTimeRemaining(timeRemaining)
                } else {
                    formatTimeElapsed(timeElapsed)
                },
                style = SimpleText.copy(
                    fontSize = 42.sp,
                    fontWeight = Bold,
                    color = if (isWarning) Color(0xFFFFA500) else Color.White
                )
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onStop, modifier = Modifier.width(120.dp)) {
                Text(stringResource(R.string.stop), style = H1.copy(color = Color.White))
            }
        }
    }
}

@Composable
fun TurboCompletionScreen(
    event: Event?,
    onContinue: () -> Unit
) {
    val duration = event?.let { (it.endTime ?: 0) - it.startTime } ?: 0
    val minutes = duration / (1000 * 60)
    val seconds = (duration / 1000) % 60
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.congratulations),
            style = H1,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.task_completed, event?.name ?: ""),
            style = H2,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (duration > 0) {
            Text(
                text = stringResource(R.string.completed_in_time, 
                    if (minutes > 0) {
                        if (seconds > 0) {
                            "$minutes ${if (minutes == 1L) "minute" else "minutes"} $seconds seconds"
                        } else {
                            "$minutes ${if (minutes == 1L) "minute" else "minutes"}"
                        }
                    } else {
                        "$seconds seconds"
                    }
                ),
                style = H2,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.width(200.dp)
        ) {
            Text(stringResource(R.string.continue_text), style = H2.copy(color = Color.White))
        }
    }
}

@Composable
fun TurboButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .width(86.dp)
            .clip(RoundedCornerShape(50.dp))
            .shadow(2.dp)
            .background(
                color = if (enabled) Color(0xFF33BA78) else Color(0xFF33BA78).copy(alpha = 0.5f),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.lightning),
            contentDescription = null,
            modifier = Modifier.size(16.dp, 22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.go), 
            fontSize = 18.sp, 
            fontStyle = FontStyle.Italic,
            color = Color.White, 
            fontFamily = Montserrat, 
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun formatTimeElapsed(seconds: Long): String {
    val hrs = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, minutes, secs)
}

fun formatTimeRemaining(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
}