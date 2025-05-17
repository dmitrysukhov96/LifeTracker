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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.TimeTracker
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsViewModel
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
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
    setTopBarState: (TopBarState) -> Unit, trackerViewModel: TrackerViewModel,
    navController: NavHostController, projectsViewModel: ProjectsViewModel
) {
    var showTaskDialog by remember { mutableStateOf(false) }
    var showNewEventDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var isTrackerStart by remember { mutableStateOf(false) }
    val title = stringResource(R.string.tracker)
    setTopBarState(TopBarState(title, screen = TRACKER_SCREEN) {
        IconButton(onClick = {
            selectedEvent = null
            isTrackerStart = false
            showTaskDialog = true
        }) { Icon(painterResource(R.drawable.plus), null, tint = Color.White) }
    })
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val events by trackerViewModel.getEventsForDate(selectedDate).collectAsState(emptyList())
    val projects by trackerViewModel.projects.collectAsState()
    val lastEvent = trackerViewModel.lastEvent.collectAsState().value
    val lastProjectId by projectsViewModel.lastCreatedProjectId.collectAsState(null)
    var selectedProjectId by remember { mutableStateOf(selectedEvent?.projectId) }
    val context = LocalContext.current

    LaunchedEffect(lastProjectId) {
        lastProjectId?.let {
            showTaskDialog = true
            selectedProjectId = it
            projectsViewModel.clearLastCreatedProjectId()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000); refreshTrigger += 1
        }
    }
    LaunchedEffect(refreshTrigger, selectedDate) { trackerViewModel.refreshEvents() }
    
    // Small dialog - for starting a new event from the time tracker
    if (showNewEventDialog) {
        NewEventDialog(
            projects = projects,
            onDismiss = { showNewEventDialog = false },
            onSave = { event ->
                if (lastEvent?.endTime == null) {
                    trackerViewModel.stopEvent()
                }
                trackerViewModel.insertEvent(event)
                showNewEventDialog = false
            }, 
            selectedProjectId = selectedProjectId,
            onNavigateToNewProject = { navController.navigate(NEW_PROJECT_SCREEN) },
            setProjectId = { selectedProjectId = it }
        )
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        TimeTracker(
            lastEvent = lastEvent, projects = projects, onCircleButtonClick = {
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    showNewEventDialog = true
                } else trackerViewModel.stopEvent()
            }, onActionClick = {
                if (lastEvent == null || lastEvent.endTime != null) {
                    selectedEvent = null
                    showNewEventDialog = true
                } else showNewEventDialog = true
            }
        )
        Spacer(Modifier.height(16.dp))
        TrackerTimeline(
            events = events, selectedDate = selectedDate, onDateSelected = { selectedDate = it },
            projects = projects, onEventClick = { event ->
                if (event.endTime == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.stop_tracker_first),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@TrackerTimeline
                }
                selectedEvent = event
                isTrackerStart = false
                showTaskDialog = true
            }, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
    }
    
    // Big dialog - for adding new event from plus button or editing existing event
    if (showTaskDialog) {
        AddEditEventDialog(
            event = selectedEvent,
            projects = projects,
            onDismiss = {
                showTaskDialog = false
                selectedEvent = null
            },
            onSave = { event ->
                if (selectedEvent == null) {
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
            },
            selectedProjectId = selectedProjectId,
            onNavigateToNewProject = { navController.navigate(NEW_PROJECT_SCREEN) },
            setProjectId = { selectedProjectId = it }
        )
    }
}

@Composable
fun NewEventDialog(
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    selectedProjectId: Long?,
    onNavigateToNewProject: () -> Unit,
    setProjectId: (Long?) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var error: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(error) {
        if (!error.isNullOrEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            error = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.start_new_task),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Montserrat,
                    fontWeight = Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = taskName,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PineColor,
                        unfocusedBorderColor = InverseColor.copy(0.5f)
                    ),
                    onValueChange = { taskName = it },
                    label = {
                        Text(
                            stringResource(R.string.task_name),
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
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
                                    ).let { modifier ->
                                        if (selectedProjectId == null) {
                                            modifier.border(
                                                1.dp,
                                                InverseColor.copy(0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else modifier
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
                            painterResource(R.drawable.arrow_down),
                            contentDescription = null,
                            tint = PineColor,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
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
                                onNavigateToNewProject()
                            }
                        )
                        HorizontalDivider()
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
                                setProjectId(null)
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
                                    setProjectId(project.projectId)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (taskName.isBlank()) return@Button
                            val newEvent = Event(
                                eventId = 0,
                                projectId = selectedProjectId ?: 0,
                                name = taskName,
                                startTime = DateTime.now().millis,
                                endTime = null
                            )
                            onSave(newEvent)
                        },
                        enabled = taskName.isNotBlank(),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(stringResource(R.string.save), style = H1.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditEventDialog(
    event: Event?,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: (Event) -> Unit,
    selectedProjectId: Long?,
    onNavigateToNewProject: () -> Unit,
    setProjectId: (Long?) -> Unit
) {
    var taskName by remember { mutableStateOf(event?.name ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val now = DateTime.now()
    val startDateTime = event?.let { DateTime(it.startTime) } ?: now
    val endDateTime = event?.endTime?.let { DateTime(it) } ?: now.plusHours(1)
    var startDate by remember { mutableStateOf(startDateTime.toString("dd.MM.yyyy")) }
    var startTime by remember { mutableStateOf(startDateTime.toString("HH:mm")) }
    var endDate by remember { mutableStateOf(endDateTime.toString("dd.MM.yyyy")) }
    var endTime by remember { mutableStateOf(endDateTime.toString("HH:mm")) }
    var error: String? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(error) {
        if (!error.isNullOrEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            error = null
        }
    }

    fun validateTimeFormat(time: String): Boolean {
        return time.matches(Regex("\\d{1,2}:\\d{1,2}"))
    }
    
    fun formatTimeIfNeeded(time: String): String {
        if (time.contains(":")) return time
        
        val digits = time.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        if (digits.length <= 2) return digits
        
        // Format as HH:mm
        return "${digits.take(2)}:${digits.drop(2).take(2)}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            fun isFuture(dt: DateTime) = dt.isAfter(now)

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (event == null) stringResource(R.string.add_event) else stringResource(
                        R.string.edit_event
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = Montserrat,
                    fontWeight = Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = taskName,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PineColor,
                        unfocusedBorderColor = InverseColor.copy(0.5f)
                    ),
                    onValueChange = { taskName = it },
                    label = {
                        Text(
                            stringResource(R.string.task_name),
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier
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
                                    ).let { modifier ->
                                        if (selectedProjectId == null) {
                                            modifier.border(
                                                1.dp,
                                                InverseColor.copy(0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                        } else modifier
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
                            painterResource(R.drawable.arrow_down),
                            contentDescription = null,
                            tint = PineColor,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
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
                                onNavigateToNewProject()
                            }
                        )
                        HorizontalDivider()
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
                                setProjectId(null)
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
                                    setProjectId(project.projectId)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.start_time),
                    fontFamily = Montserrat,
                    fontWeight = Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            unfocusedBorderColor = InverseColor.copy(0.5f)
                        ),
                        onValueChange = { startDate = it },
                        label = {
                            Text(
                                stringResource(R.string.date),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = startTime,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            unfocusedBorderColor = InverseColor.copy(0.5f)
                        ),
                        onValueChange = { input -> 
                            // Allow only digits and colon
                            val filtered = input.filter { it.isDigit() || it == ':' }
                            startTime = filtered
                        },
                        label = {
                            Text(
                                stringResource(R.string.time),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        placeholder = { Text("HH:mm") }
                    )
                }

                Text(
                    text = stringResource(R.string.end_time),
                    fontFamily = Montserrat,
                    fontWeight = Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endDate,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            unfocusedBorderColor = InverseColor.copy(0.5f)
                        ),
                        onValueChange = { endDate = it },
                        label = {
                            Text(
                                stringResource(R.string.date),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = endTime,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            unfocusedBorderColor = InverseColor.copy(0.5f)
                        ),
                        onValueChange = { input ->
                            // Allow only digits and colon
                            val filtered = input.filter { it.isDigit() || it == ':' }
                            endTime = filtered
                        },
                        label = {
                            Text(
                                stringResource(R.string.time),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        placeholder = { Text("HH:mm") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (event != null) {
                        TextButton(onClick = { onDelete(event) }) {
                            Text(stringResource(R.string.delete), color = PineColor, style = H2)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = {
                            try {
                                val formattedStartTime = formatTimeIfNeeded(startTime)
                                val formattedEndTime = formatTimeIfNeeded(endTime)
                                
                                val format = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
                                val startDT = try {
                                    DateTime.parse("$startDate $formattedStartTime", format)
                                } catch (e: Exception) {
                                    error = context.getString(R.string.error_parsing_date)
                                    return@Button
                                }
                                
                                val endDT = try {
                                    DateTime.parse("$endDate $formattedEndTime", format)
                                } catch (e: Exception) {
                                    error = context.getString(R.string.error_parsing_date)
                                    return@Button
                                }
                                
                                if (isFuture(startDT) || isFuture(endDT)) {
                                    error = context.getString(R.string.cannot_create_event_in_future)
                                    return@Button
                                }
                                if (taskName.isBlank()) return@Button
                                if (endDT.isBefore(startDT)) {
                                    error = context.getString(R.string.error_end_time_before_start)
                                    return@Button
                                }
                                val updatedEvent = Event(
                                    eventId = event?.eventId ?: 0,
                                    projectId = selectedProjectId ?: 0,
                                    name = taskName,
                                    startTime = startDT.millis,
                                    endTime = endDT.millis
                                )
                                onSave(updatedEvent)
                            } catch (e: Exception) {
                                error = context.getString(R.string.error_parsing_date)
                            }
                        },
                        enabled = taskName.isNotBlank(),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(stringResource(R.string.save), style = H1.copy(color = Color.White))
                    }
                }
            }
        }
    }
}

const val TRACKER_SCREEN = "Tracker"