package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.todo.ProjectTag
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale

@Composable
fun TrackerScreen(
    setTopBarState: (TopBarState) -> Unit,
    trackerViewModel: TrackerViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val events by trackerViewModel.getEventsForDate(selectedDate).collectAsState(initial = emptyList())
    val projects by trackerViewModel.projects.collectAsState()
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("Tracker") {
            IconButton(onClick = { 
                selectedEvent = null
                showTaskDialog = true 
            }) {
                Icon(
                    painterResource(R.drawable.plus),
                    contentDescription = null, tint = Color.White
                )
            }
        })
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
            projects = projects,
            onEventClick = { event -> 
                selectedEvent = event
                showTaskDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }

    if (showTaskDialog) {
        EventDialog(
            event = selectedEvent,
            projects = projects,
            onDismiss = { 
                showTaskDialog = false
                selectedEvent = null
            },
            onSave = { event ->
                if (event.eventId == 0L) {
                    trackerViewModel.insertEvent(event)
                } else {
                    trackerViewModel.updateEvent(event)
                }
                showTaskDialog = false
                selectedEvent = null
            },
            onDelete = { event ->
                trackerViewModel.deleteEvent(event.eventId)
                showTaskDialog = false
                selectedEvent = null
            }
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
    
    val backgroundColor by animateColorAsState(
        targetValue = if (lastEvent?.endTime == null) AccentColor else Color(0xFFC2EBD6),
        animationSpec = tween(durationMillis = 300)
    )

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
                        fontFamily = Montserrat,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Name", fontFamily = Montserrat) },
                        textStyle = TextStyle(fontFamily = Montserrat),
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
                                } ?: "Select Project",
                                fontFamily = Montserrat
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.title, fontFamily = Montserrat) },
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
                            Text("Cancel", fontFamily = Montserrat)
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
                            Text("OK", fontFamily = Montserrat)
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
            .background(backgroundColor)
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
            if (lastEvent?.endTime == null) {
                lastEvent?.projectId?.let { projectId ->
                    projects.find { it.projectId == projectId }?.let { project ->
                        ProjectTag(text = project.title, color = Color(project.color))
                    }
                }
            }
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

@Composable
fun EventDialog(
    event: Event?,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit
) {
    var taskName by remember { mutableStateOf(event?.name ?: "") }
    var selectedProjectId by remember { mutableStateOf(event?.projectId) }
    var expanded by remember { mutableStateOf(false) }
    
    val startDateTime = event?.let { DateTime(it.startTime) } ?: DateTime.now()
    val endDateTime = event?.endTime?.let { DateTime(it) } ?: DateTime.now()
    
    var startDate by remember { mutableStateOf(startDateTime.toString("dd.MM.yyyy")) }
    var startTime by remember { mutableStateOf(startDateTime.toString("HH:mm")) }
    var endDate by remember { mutableStateOf(endDateTime.toString("dd.MM.yyyy")) }
    var endTime by remember { mutableStateOf(endDateTime.toString("HH:mm")) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = if (event == null) "New Event" else "Edit Event",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name", fontFamily = Montserrat) },
                    textStyle = TextStyle(fontFamily = Montserrat),
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
                            } ?: "Select Project",
                            fontFamily = Montserrat
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        projects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.title, fontFamily = Montserrat) },
                                onClick = {
                                    selectedProjectId = project.projectId
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Start Date and Time
                Text(
                    text = "Start",
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Date (dd.mm.yyyy)", fontFamily = Montserrat) },
                        textStyle = TextStyle(fontFamily = Montserrat),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Time (hh:mm)", fontFamily = Montserrat) },
                        textStyle = TextStyle(fontFamily = Montserrat),
                        modifier = Modifier.weight(1f)
                    )
                }

                // End Date and Time
                Text(
                    text = "End",
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Date (dd.mm.yyyy)", fontFamily = Montserrat) },
                        textStyle = TextStyle(fontFamily = Montserrat),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Time (hh:mm)", fontFamily = Montserrat) },
                        textStyle = TextStyle(fontFamily = Montserrat),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (event != null) {
                        IconButton(
                            onClick = { onDelete(event) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.delete),
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = Montserrat)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (taskName.isBlank() || selectedProjectId == null) {
                                return@Button
                            }
                            try {
                                val startDateTime = DateTime.parse("$startDate $startTime", DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"))
                                val endDateTime = DateTime.parse("$endDate $endTime", DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"))
                                
                                if (endDateTime.isBefore(startDateTime)) {
                                    // Показать ошибку
                                    return@Button
                                }
                                
                                val newEvent = Event(
                                    eventId = event?.eventId ?: 0,
                                    projectId = selectedProjectId!!,
                                    name = taskName,
                                    startTime = startDateTime.millis,
                                    endTime = endDateTime.millis
                                )
                                onSave(newEvent)
                            } catch (_: Exception) {
                                // Показать ошибку формата
                            }
                        },
                        enabled = taskName.isNotBlank() && selectedProjectId != null
                    ) {
                        Text("Save", fontFamily = Montserrat)
                    }
                }
            }
        }
    }
}

const val TRACKER_SCREEN = "Tracker"