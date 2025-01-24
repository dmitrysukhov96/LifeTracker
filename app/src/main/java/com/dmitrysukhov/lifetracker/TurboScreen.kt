package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurboModeScreen(
    onClose: () -> Unit
) {
    var selectedTask by remember { mutableStateOf<String?>(null) }
    var isCountdown by remember { mutableStateOf(true) }
    var minutes by remember { mutableStateOf("45") }
    var seconds by remember { mutableStateOf("00") }
    var isRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(45.minutes) }

    LaunchedEffect(isRunning, timeLeft) {
        while (isRunning && timeLeft > 0.seconds) {
            delay(1000)
            timeLeft -= 1.seconds
        }
        if (timeLeft <= 0.seconds) {
            isRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Turbo Mode") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(painterResource(id = R.drawable.ic_chevron_left), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!isRunning) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Task",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedTask?.let {
                            Text(it)
                        } ?: Text("No task selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column {
                    Text(
                        text = "Timer Mode",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = isCountdown,
                            onClick = { isCountdown = true }
                        )
                        Text("Countdown")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = !isCountdown,
                            onClick = { isCountdown = false }
                        )
                        Text("Stopwatch")
                    }
                }

                if (isCountdown) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it },
                            modifier = Modifier.width(80.dp),
                            label = { Text("Minutes") }
                        )
                        Text(":", modifier = Modifier.padding(horizontal = 8.dp))
                        OutlinedTextField(
                            value = seconds,
                            onValueChange = { seconds = it },
                            modifier = Modifier.width(80.dp),
                            label = { Text("Seconds") }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatDuration(timeLeft),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            Button(
                onClick = {
                    if (!isRunning) {
                        timeLeft = minutes.toIntOrNull()?.minutes ?: 45.minutes +
                                seconds.toIntOrNull()?.seconds!! ?: 0.seconds
                    }
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (isRunning) "Stop" else "GO!")
            }
        }
    }
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

data class PlanningTask(
    val id: String,
    val title: String,
    val selected: Boolean = false,
    val startTime: String? = null,
    val duration: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlanningScreen(
    onClose: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var tasks by remember {
        mutableStateOf(
            listOf(
                PlanningTask("1", "Побегать"),
                PlanningTask("2", "Помыть пол"),
                PlanningTask("3", "Испечь торт"),
                PlanningTask("4", "Сделать уроки"),
                PlanningTask("5", "Покормить кота")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Planning") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(painterResource(id = R.drawable.ic_chevron_left), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (currentStep) {
                0 -> TaskSelectionStep(
                    tasks = tasks,
                    onTaskToggle = { taskId ->
                        tasks = tasks.map {
                            if (it.id == taskId) it.copy(selected = !it.selected)
                            else it
                        }
                    },
                    onNext = { currentStep++ }
                )
                1 -> TimeAllocationStep(
                    tasks = tasks.filter { it.selected },
                    onTaskTimeSet = { taskId, startTime, duration ->
                        tasks = tasks.map {
                            if (it.id == taskId) it.copy(startTime = startTime, duration = duration)
                            else it
                        }
                    },
                    onNext = { currentStep++ }
                )
                2 -> FinalPlanStep(
                    tasks = tasks.filter { it.selected },
                    onFinish = onClose
                )
            }
        }
    }
}

@Composable
fun TaskSelectionStep(
    tasks: List<PlanningTask>,
    onTaskToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Select tasks for today",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.selected,
                        onCheckedChange = { onTaskToggle(task.id) }
                    )
                    Text(task.title)
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Next")
        }
    }
}

@Composable
fun TimeAllocationStep(
    tasks: List<PlanningTask>,
    onTaskTimeSet: (String, String, String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Set time for tasks",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tasks) { task ->
                TaskTimeAllocation(
                    task = task,
                    onTimeSet = { startTime, duration ->
                        onTaskTimeSet(task.id, startTime, duration)
                    }
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Create Plan")
        }
    }
}

@Composable
fun TaskTimeAllocation(
    task: PlanningTask,
    onTimeSet: (String, String) -> Unit
) {
    var startTime by remember { mutableStateOf(task.startTime ?: "") }
    var duration by remember { mutableStateOf(task.duration ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {
                        startTime = it
                        onTimeSet(it, duration)
                    },
                    label = { Text("Start Time") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        duration = it
                        onTimeSet(startTime, it)
                    },
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FinalPlanStep(
    tasks: List<PlanningTask>,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Your Plan for Today",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(task.title)
                        Text("${task.startTime} (${task.duration})")
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Done")
        }
    }
}