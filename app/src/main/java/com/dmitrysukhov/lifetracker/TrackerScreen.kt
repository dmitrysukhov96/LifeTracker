package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.joda.time.DateTime

data class TrackerTask(
    val title: String,
    val project: String,
    val startTime: String,
    val duration: String,
    val color: Color
)

val trackerTasks = listOf(
    TrackerTask("Сходить на базар", "Покупки", "8:30", "0:45", Color(0xFFFF9800)),
    TrackerTask("Сделать обложки", "Лайф", "10:00", "1:17", Color(0xFF2196F3)),
    TrackerTask("Чебуреки с подростками", "Лайф", "13:00", "1:40", Color(0xFFF44336)),
    TrackerTask("Дизайн LifeTracker", "Дизайн", "14:00", "0:50", Color(0xFF2AC17C))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen() {
    var selectedDate by remember { mutableStateOf(DateTime()) }
    var currentTask by remember { mutableStateOf<TrackerTask?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracker") },
                actions = {
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DateSelector(selectedDate) { newDate ->
                selectedDate = newDate
            }

            CurrentTaskTracker(currentTask)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trackerTasks) { task ->
                    TrackerTaskItem(task) {
                        currentTask = if (currentTask == task) null else task
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(selectedDate: DateTime, onDateSelected: (DateTime) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) {
            Icon(painterResource(id = R.drawable.ic_chevron_left), contentDescription = "Previous day")
        }
        Text(
            text = selectedDate.toString("d MMMM yyyy"),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { onDateSelected(selectedDate.plusDays(1)) }) {
            Icon(painterResource(id = R.drawable.ic_chevron_right), contentDescription = "Next day")
        }
    }
}

@Composable
fun CurrentTaskTracker(task: TrackerTask?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task?.title ?: "Без задачи...",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "00:59:38",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = { /* Handle start/stop */ },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    .size(40.dp)
            ) {
                Icon(
                    painterResource(id = if (task == null) R.drawable.play else R.drawable.stop),
                    contentDescription = if (task == null) "Start" else "Pause",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun TrackerTaskItem(task: TrackerTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = task.color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.startTime,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.project,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = task.duration,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onClick) {
                Icon(
                    painterResource(id = R.drawable.play),
                    contentDescription = "Start task",
                    tint = task.color
                )
            }
        }
    }
}
