package com.dmitrysukhov.lifetracker.tracker

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.TimeTracker
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

@Composable
fun TrackerScreen(
    setTopBarState: (TopBarState) -> Unit, trackerViewModel: TrackerViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val events by trackerViewModel.getEventsForDate(selectedDate).collectAsState(emptyList())
    val projects by trackerViewModel.projects.collectAsState()
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var isTrackerStart by remember { mutableStateOf(false) }
    val title = stringResource(R.string.tracker)
    val lastEvent = trackerViewModel.lastEvent.collectAsState().value
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000); refreshTrigger += 1
        }
    }
    
    LaunchedEffect(refreshTrigger, selectedDate) { 
        trackerViewModel.refreshEvents() 
    }
    
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(title) {
            IconButton(onClick = {
                selectedEvent = null
                isTrackerStart = false
                showTaskDialog = true
            }) {
                Icon(
                    painterResource(R.drawable.plus), contentDescription = null, tint = Color.White
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
            lastEvent = lastEvent,
            projects = projects,
            onActionClick = {
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    isTrackerStart = true
                    showTaskDialog = true
                } else {
                    trackerViewModel.stopEvent()
                }
            }
        )
        
        Spacer(Modifier.height(16.dp))
        
        TrackerTimeline(
            events = events, 
            selectedDate = selectedDate, 
            onDateSelected = { selectedDate = it },
            projects = projects, 
            onEventClick = { event ->
                selectedEvent = event
                isTrackerStart = false
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
                if (event.eventId == 0L) trackerViewModel.insertEvent(event)
                else trackerViewModel.updateEvent(event)
                showTaskDialog = false
                selectedEvent = null
            }, 
            onDelete = { event ->
                trackerViewModel.deleteEvent(event.eventId)
                showTaskDialog = false
                selectedEvent = null
            }, 
            trackerStart = isTrackerStart, 
            navController = navController
        )
    }
}

@Composable
fun EventDialog(
    event: Event?, projects: List<Project>, onDismiss: () -> Unit, onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit, trackerStart: Boolean = false, navController: NavHostController
) {
    var taskName by remember { mutableStateOf(event?.name ?: "") }
    var selectedProjectId by remember { mutableStateOf(event?.projectId) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
                .padding(16.dp), shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = when {
                        trackerStart -> stringResource(R.string.start_new_task)
                        event == null -> stringResource(R.string.new_event)
                        else -> stringResource(R.string.edit_event)
                    }, style = MaterialTheme.typography.titleLarge, fontFamily = Montserrat,
                    fontWeight = Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = taskName, colors = colors(
                        focusedBorderColor = PineColor,
                        unfocusedBorderColor = InverseColor.copy(0.5f)
                    ), onValueChange = { taskName = it }, label = {
                        Text(
                            stringResource(R.string.task_name), fontFamily = Montserrat,
                            fontSize = 16.sp, fontWeight = FontWeight.Medium
                        )
                    }, textStyle = TextStyle(
                        fontFamily = Montserrat, fontSize = 16.sp, fontWeight = FontWeight.Medium
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        Modifier
                            .clickable { expanded = true }
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                if (expanded) 2.dp else 1.dp,
                                if (expanded) PineColor else InverseColor.copy(0.5f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        selectedProjectId?.let { id ->
                                            projects.find { it.projectId == id }?.let { project ->
                                                Color(project.color)
                                            } ?: Color.Transparent
                                        } ?: Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .let { modifier ->
                                        if (selectedProjectId == null) {
                                            modifier.border(
                                                1.dp,
                                                InverseColor.copy(0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else {
                                            modifier
                                        }
                                    }
                            )
                            Text(
                                text = selectedProjectId?.let { id ->
                                    projects.find { it.projectId == id }?.title
                                        ?: stringResource(R.string.select_project)
                                } ?: stringResource(R.string.no_project),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = InverseColor
                            )
                        }
                        Icon(
                            painterResource(R.drawable.arrow_down), contentDescription = null,
                            tint = PineColor, modifier = Modifier
                                .padding(top = 1.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(BgColor)
                            .padding(horizontal = 8.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.plus),
                                        contentDescription = null,
                                        tint = PineColor
                                    )
                                    Text(
                                        stringResource(R.string.add_project),
                                        fontFamily = Montserrat,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PineColor
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                navController.navigate(NEW_PROJECT_SCREEN)
                            }
                        )
                        HorizontalDivider()
                        // No project option
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color.Transparent, RoundedCornerShape(4.dp))
                                            .border(
                                                1.dp,
                                                InverseColor.copy(0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Text(
                                        stringResource(R.string.no_project),
                                        fontFamily = Montserrat,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = InverseColor
                                    )
                                }
                            },
                            onClick = {
                                selectedProjectId = null
                                expanded = false
                            }
                        )
                        projects.forEach { project ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    Color(project.color),
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                        Text(
                                            project.title,
                                            fontFamily = Montserrat,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = InverseColor
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProjectId = project.projectId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                if (!trackerStart) {
                    Text(
                        text = stringResource(R.string.start), fontFamily = Montserrat,
                        fontWeight = Bold, fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startDate, colors = colors(
                                focusedBorderColor = PineColor,
                                unfocusedBorderColor = InverseColor.copy(0.5f)
                            ), onValueChange = { startDate = it }, label = {
                                Text(
                                    stringResource(R.string.date), fontFamily = Montserrat,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                            }, textStyle = TextStyle(
                                fontFamily = Montserrat, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ), modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startTime, colors = colors(
                                focusedBorderColor = PineColor,
                                unfocusedBorderColor = InverseColor.copy(0.5f)
                            ), onValueChange = { startTime = it }, label = {
                                Text(
                                    stringResource(R.string.time), fontFamily = Montserrat,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                            }, textStyle = TextStyle(
                                fontFamily = Montserrat, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ), modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.end), fontFamily = Montserrat,
                        fontWeight = Bold, fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = endDate, colors = colors(
                                focusedBorderColor = PineColor,
                                unfocusedBorderColor = InverseColor.copy(0.5f)
                            ), onValueChange = { endDate = it }, label = {
                                Text(
                                    stringResource(R.string.date), fontFamily = Montserrat,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                            }, textStyle = TextStyle(
                                fontFamily = Montserrat, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ), modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime, colors = colors(
                                focusedBorderColor = PineColor,
                                unfocusedBorderColor = InverseColor.copy(0.5f)
                            ), onValueChange = { endTime = it }, label = {
                                Text(
                                    stringResource(R.string.time), fontFamily = Montserrat,
                                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                                )
                            }, textStyle = TextStyle(
                                fontFamily = Montserrat, fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ), modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), horizontalArrangement = Arrangement.End
                ) {
                    if (event != null && !trackerStart) {
                        IconButton(
                            onClick = { onDelete(event) }, modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.delete), tint = InverseColor,
                                contentDescription = stringResource(R.string.delete),
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.cancel), fontFamily = Montserrat,
                            fontSize = 16.sp, fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (taskName.isBlank()) return@Button
                            if (trackerStart) {
                                val newEvent = Event(
                                    eventId = 0, projectId = selectedProjectId ?: 0,
                                    name = taskName, startTime = DateTime.now().millis,
                                    endTime = null
                                )
                                onSave(newEvent)
                            } else {
                                try {
                                    val startDateTime = DateTime.parse(
                                        "$startDate $startTime",
                                        DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
                                    )
                                    val endDateTime = DateTime.parse(
                                        "$endDate $endTime",
                                        DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
                                    )

                                    if (endDateTime.isBefore(startDateTime)) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_end_time_before_start),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    val newEvent = Event(
                                        eventId = event?.eventId ?: 0,
                                        projectId = selectedProjectId ?: 0, name = taskName,
                                        startTime = startDateTime.millis,
                                        endTime = endDateTime.millis
                                    )
                                    onSave(newEvent)
                                } catch (e: Exception) {
                                    when (e) {
                                        is IllegalArgumentException -> {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_invalid_date_format),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_parsing_date),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }, enabled = taskName.isNotBlank()
                    ) {
                        Text(
                            stringResource(R.string.save), fontFamily = Montserrat,
                            fontSize = 16.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

const val TRACKER_SCREEN = "Tracker"