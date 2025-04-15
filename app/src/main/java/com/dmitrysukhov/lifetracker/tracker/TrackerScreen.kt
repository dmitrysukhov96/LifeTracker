package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay
import org.joda.time.LocalDate
import java.util.Locale

@Composable
fun TrackerScreen(
    setTopBarState: (TopBarState) -> Unit,
    trackerViewModel: TrackerViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val events by trackerViewModel.getEventsForDate(selectedDate).collectAsState(initial = emptyList())
    var showTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("Tracker"))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        TimeTracker(
            trackerViewModel = trackerViewModel,
            showTaskDialog = showTaskDialog,
            onShowTaskDialogChange = { showTaskDialog = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TrackerTimeline(
            events = events,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun TimeTracker(
    trackerViewModel: TrackerViewModel,
    showTaskDialog: Boolean,
    onShowTaskDialogChange: (Boolean) -> Unit
) {
    val lastEvent by trackerViewModel.lastEvent.collectAsState()
    val projects by trackerViewModel.projects.collectAsState()
    var timeElapsed by remember { mutableLongStateOf(0L) }
    var taskName by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(lastEvent) {
        while (true) {
            timeElapsed = when {
                lastEvent == null -> 0L
                lastEvent?.endTime == null -> (System.currentTimeMillis() - (lastEvent?.startTime ?: 0)) / 1000
                else -> (System.currentTimeMillis() - lastEvent?.endTime!!) / 1000
            }
            delay(1000)
        }
    }

    if (showTaskDialog) {
        Dialog(onDismissRequest = { onShowTaskDialogChange(false) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (lastEvent == null) "Start New Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedProjectId?.let { id ->
                                    projects.find { it.projectId == id }?.title ?: "Select Project"
                                } ?: "Select Project"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.title) },
                                    onClick = {
                                        selectedProjectId = project.projectId
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onShowTaskDialogChange(false) }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (taskName.isBlank()) {
                                    return@Button
                                }
                                if (lastEvent != null) {
                                    trackerViewModel.stopEvent()
                                }
                                selectedProjectId?.let { projectId ->
                                    trackerViewModel.startEvent(projectId, taskName)
                                }
                                onShowTaskDialogChange(false)
                            },
                            enabled = taskName.isNotBlank() && selectedProjectId != null
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(AccentColor)
            .padding(horizontal = 20.dp)
            .clickable { 
                taskName = lastEvent?.name ?: ""
                selectedProjectId = lastEvent?.projectId
                onShowTaskDialogChange(true) 
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                if (lastEvent?.endTime == null) lastEvent?.name ?: "Без задачи..." else "Без задачи...",
                color = BlackPine,
                fontWeight = Bold,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (lastEvent?.endTime == null) {
                    lastEvent?.projectId?.let { projectId ->
                        projects.find { it.projectId == projectId }?.title ?: ""
                    } ?: ""
                } else "",
                color = BlackPine.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        val timeText = when {
            lastEvent == null -> formatTimeElapsed(timeElapsed)  // Время без задачи
            lastEvent?.endTime == null -> formatTimeElapsed(timeElapsed)  // Событие идет
            else -> formatTimeElapsed(timeElapsed)  // Время без задачи
        }

        Text(
            timeText,
            color = BlackPine,
            fontWeight = Bold,
            fontFamily = Montserrat,
            fontSize = 20.sp,
            modifier = Modifier
                .width(130.dp)
                .padding(horizontal = 12.dp)
        )

        Row {
            Box(
                modifier = Modifier
                    .clickable {
                        if (lastEvent == null || lastEvent?.endTime != null) {
                            taskName = ""
                            selectedProjectId = null
                            onShowTaskDialogChange(true)
                        } else {
                            trackerViewModel.stopEvent()
                        }
                    }
                    .clip(CircleShape)
                    .background(PineColor)
                    .size(45.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (lastEvent?.endTime == null) R.drawable.stop else R.drawable.play
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(21.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

fun formatTimeElapsed(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(),"%02d:%02d:%02d", hrs, mins, secs)
}

const val TRACKER_SCREEN = "Tracker"