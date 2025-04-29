package com.dmitrysukhov.lifetracker.turbo

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

//import com.dmitrysukhov.lifetracker.FAB_EXPLODE_BOUNDS_KEY

//@OptIn(ExperimentalSharedTransitionApi::class)
//@Composable
//fun SharedTransitionScope.TurboScreen(
//    animatedVisibilityScope: AnimatedVisibilityScope,
//) {
//    Column(
//        modifier = Modifier
//            .background(PineColor)
//            .fillMaxSize()
//            .sharedBounds(
//                sharedContentState = rememberSharedContentState(
//                    key = FAB_EXPLODE_BOUNDS_KEY
//                ),
//                animatedVisibilityScope = animatedVisibilityScope
//            )
//    ) {
//        Text("Main content")
//    }
//}

const val TURBO_SCREEN = "turbo_screen"

@Composable
fun TurboScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    viewModel: TurboViewModel = hiltViewModel()
) {
    val todoList by viewModel.todoList.collectAsState()
    val session by viewModel.turboSession.collectAsState()
    val context = LocalContext.current
    
    // UI state
    var showTaskDialog by remember { mutableStateOf(false) }
    var newTaskText by remember { mutableStateOf("") }
    var showTaskDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = context.getString(R.string.turbo_mode),
                color = PineColor
            )
        )
    }
    
    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PineColor, AccentColor)
                )
            )
    ) {
        when (session.timerState) {
            TimerState.IDLE -> {
                TurboSetupScreen(
                    todoList = todoList,
                    session = session,
                    onTaskSelect = { viewModel.selectTask(it) },
                    onModeSelect = { viewModel.setTimerMode(it) },
                    onDurationChange = { viewModel.setDuration(it) },
                    onStart = { viewModel.startTimer() },
                    onAddNewTask = {
                        newTaskText = ""
                        showTaskDialog = true
                    },
                    onDropdownToggle = { showTaskDropdown = it }
                )
            }
            TimerState.RUNNING, TimerState.PAUSED -> {
                TurboActiveScreen(
                    session = session,
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() },
                    onStop = { viewModel.stopTimer() }
                )
            }
            TimerState.COMPLETED -> {
                TurboCompletedScreen(
                    session = session,
                    todoList = todoList,
                    onTaskSelect = { viewModel.selectTask(it) },
                    onModeSelect = { viewModel.setTimerMode(it) },
                    onDurationChange = { viewModel.setDuration(it) },
                    onStart = { viewModel.startTimer() },
                    onAddNewTask = {
                        newTaskText = ""
                        showTaskDialog = true
                    },
                    onDropdownToggle = { showTaskDropdown = it }
                )
            }
        }
        
        if (showTaskDialog) {
            AlertDialog(
                containerColor = BgColor,
                onDismissRequest = { showTaskDialog = false },
                title = { Text(text = stringResource(R.string.add_task), style = H1) },
                text = {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        label = { Text(stringResource(R.string.task_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                viewModel.addTask(newTaskText)
                                showTaskDialog = false
                                newTaskText = ""
                                Toast.makeText(context, context.getString(R.string.task_added), Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = newTaskText.isNotBlank()
                    ) {
                        Text(stringResource(R.string.add_task))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTaskDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun TurboSetupScreen(
    todoList: List<TodoItem>,
    session: TurboSession,
    onTaskSelect: (TodoItem?) -> Unit,
    onModeSelect: (TimerMode) -> Unit,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onAddNewTask: () -> Unit,
    onDropdownToggle: (Boolean) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("00") }
    var minutes by remember { mutableStateOf("45") }
    
    LaunchedEffect(session.durationMinutes) {
        hours = (session.durationMinutes / 60).toString().padStart(2, '0')
        minutes = (session.durationMinutes % 60).toString().padStart(2, '0')
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
        
        // Task dropdown
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        showDropdown = true
                        onDropdownToggle(true)
                    },
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
                        text = session.selectedTask?.text
                            ?: (stringResource(R.string.select_task) + "..."),
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
                expanded = showDropdown,
                onDismissRequest = { 
                    showDropdown = false
                    onDropdownToggle(false)
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(BgColor)
            ) {
                DropdownMenuItem(
                    text = { Text("+ Добавить задачу") },
                    onClick = {
                        onAddNewTask()
                        showDropdown = false
                        onDropdownToggle(false)
                    }
                )
                
                todoList.forEach { task ->
                    DropdownMenuItem(
                        text = { Text(task.text) },
                        onClick = {
                            onTaskSelect(task)
                            showDropdown = false
                            onDropdownToggle(false)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!showDropdown) {
            // Timer settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.timer_settings) + ":",
                    style = SimpleText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = session.timerMode == TimerMode.STOPWATCH,
                    onClick = { onModeSelect(TimerMode.STOPWATCH) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White,
                        unselectedColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = stringResource(R.string.stopwatch),
                    style = SimpleText,
                    color = Color.White,
                    modifier = Modifier.clickable { onModeSelect(TimerMode.STOPWATCH) }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                RadioButton(
                    selected = session.timerMode == TimerMode.COUNTDOWN,
                    onClick = { onModeSelect(TimerMode.COUNTDOWN) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color.White,
                        unselectedColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = stringResource(R.string.countdown),
                    style = SimpleText,
                    color = Color.White,
                    modifier = Modifier.clickable { onModeSelect(TimerMode.COUNTDOWN) }
                )
            }
            
            if (session.timerMode == TimerMode.COUNTDOWN) {
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
                            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                                hours = newValue.padStart(2, '0')
                                val hoursVal = newValue.toIntOrNull() ?: 0
                                val minutesVal = minutes.toIntOrNull() ?: 0
                                onDurationChange(hoursVal * 60 + minutesVal)
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        textStyle = SimpleText.copy(
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        label = { Text(stringResource(R.string.hours), color = Color.White.copy(alpha = 0.7f)) }
                    )
                    
                    Text(
                        text = ":",
                        style = H1,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Minutes
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { newValue ->
                            if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                                val minValue = newValue.toIntOrNull() ?: 0
                                if (minValue <= 59) {
                                    minutes = newValue.padStart(2, '0')
                                    val hoursVal = hours.toIntOrNull() ?: 0
                                    onDurationChange(hoursVal * 60 + minValue)
                                }
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        textStyle = SimpleText.copy(
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        label = { Text(stringResource(R.string.minutes), color = Color.White.copy(alpha = 0.7f)) }
                    )
                }
            }
        }
        
        // GO! Button
        Spacer(modifier = Modifier.height(32.dp))
        TurboButton(onClick = onStart)
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun TurboActiveScreen(
    session: TurboSession,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val formattedTime = formatTime(session.currentTimeMillis)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Task name at the top
        Text(
            text = session.selectedTask?.text ?: "Фокус-сессия",
            style = H1,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp)
        )
        
        // Timer display
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
                text = formattedTime,
                style = SimpleText.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (session.timerState == TimerState.RUNNING) {
                // Pause button
                Button(
                    onClick = onPause,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(stringResource(R.string.pause))
                }
                
                // Stop button
                Button(
                    onClick = onStop,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(stringResource(R.string.stop))
                }
            } else {
                // Resume button
                Button(
                    onClick = onResume,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(stringResource(R.string.resume))
                }
                
                // Stop button
                Button(
                    onClick = onStop,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(stringResource(R.string.complete))
                }
            }
        }
    }
}

@Composable
fun TurboCompletedScreen(
    session: TurboSession,
    todoList: List<TodoItem>,
    onTaskSelect: (TodoItem?) -> Unit,
    onModeSelect: (TimerMode) -> Unit,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onAddNewTask: () -> Unit,
    onDropdownToggle: (Boolean) -> Unit
) {
    val sessionDuration = if (session.timerMode == TimerMode.COUNTDOWN) {
        TimeUnit.MINUTES.toMillis(session.durationMinutes.toLong())
    } else {
        session.currentTimeMillis
    }
    
    val context = LocalContext.current
    
    // Increment focus session count when this screen is shown
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("user_stats", android.content.Context.MODE_PRIVATE)
        val currentCount = sharedPref.getInt("focus_sessions_count", 0)
        sharedPref.edit { putInt("focus_sessions_count", currentCount + 1) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.task_completed),
                style = H1,
                color = Color.White
            )
            
            Icon(
                painter = painterResource(R.drawable.checkmark),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Completion message
        Text(
            text = stringResource(
                R.string.task_completed_in,
                session.selectedTask?.text ?: stringResource(R.string.focus_session),
                formatTime(sessionDuration)
            ),
            style = SimpleText,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.what_next),
            style = H2,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Reuse the setup UI for "what's next"
        TurboSetupScreen(
            todoList = todoList,
            session = TurboSession(), // Reset session for next task
            onTaskSelect = onTaskSelect,
            onModeSelect = onModeSelect,
            onDurationChange = onDurationChange,
            onStart = onStart,
            onAddNewTask = onAddNewTask,
            onDropdownToggle = onDropdownToggle
        )
        
        // No need for the GO button as it's included in TurboSetupScreen
    }
}

@Composable
fun TurboButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .width(86.dp)
            .clip(RoundedCornerShape(50.dp))
            .shadow(2.dp)
            .background(color = Color(0xFF33BA78), shape = RoundedCornerShape(50.dp))
            .clickable { onClick() }
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

// Utility function to format time
fun formatTime(timeMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}