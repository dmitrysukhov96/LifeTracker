package com.dmitrysukhov.lifetracker.todo

import android.view.ContextThemeWrapper
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.isDarkTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NewTaskScreen(
    setTopBarState: (TopBarState) -> Unit, viewModel: TodoViewModel,
    navController: NavHostController
) {
    var title by rememberSaveable { mutableStateOf(viewModel.selectedTask?.text ?: "") }
    var description by rememberSaveable {
        mutableStateOf(viewModel.selectedTask?.description ?: "")
    }
    var selectedProjectId by rememberSaveable { mutableStateOf(viewModel.selectedTask?.projectId) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var deadline by rememberSaveable { mutableStateOf(viewModel.selectedTask?.dateTime) }
    var showDurationPicker by rememberSaveable { mutableStateOf(false) }
    var estimatedDurationMs by rememberSaveable {
        mutableLongStateOf(viewModel.selectedTask?.estimatedDurationMs ?: 0L)
    }
    var repeatInterval by rememberSaveable { mutableStateOf(viewModel.selectedTask?.repeatInterval) }
    var showRepeatOptions by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState(listOf())
    val isEditing = viewModel.selectedTask != null
    val topBarTitle =
        if (isEditing) stringResource(R.string.edit_task) else stringResource(R.string.new_task)
    val saveToastText =
        if (isEditing) stringResource(R.string.update_task_toast) else stringResource(R.string.save_task_toast)

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = topBarTitle, topBarActions = {
                    Row {
                        if (isEditing) IconButton({
                            viewModel.selectedTask?.let { task ->
                                viewModel.deleteTask(task)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.task_deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigateUp()
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.delete), tint = Color.White,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }

                        if (title.isNotEmpty()) {
                            IconButton({
                                if (isEditing) {
                                    viewModel.selectedTask?.let { task ->
                                        viewModel.updateTask(
                                            task.copy(
                                                text = title, description = description,
                                                projectId = selectedProjectId, dateTime = deadline,
                                                repeatInterval = repeatInterval,
                                                estimatedDurationMs = if (estimatedDurationMs > 0) estimatedDurationMs else null
                                            )
                                        )
                                    }
                                } else viewModel.addTask(
                                    text = title, description = description,
                                    projectId = selectedProjectId, deadline = deadline,
                                    repeatInterval = repeatInterval,
                                    estimatedDurationMs = if (estimatedDurationMs > 0) estimatedDurationMs else null
                                )
                                Toast.makeText(context, saveToastText, Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.tick),
                                    contentDescription = null, tint = Color.White,
                                )
                            }
                        }
                    }
                }
            )
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = title, onValueChange = { title = it }, cursorBrush = SolidColor(PineColor),
            textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) Text(
                        stringResource(R.string.title_hint),
                        style = H1.copy(color = PineColor.copy(0.5f))
                    )
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = PineColor.copy(0.5f), thickness = 0.5.dp)
        BasicTextField(
            value = description, onValueChange = { description = it },
            textStyle = SimpleText.copy(color = InverseColor),
            cursorBrush = SolidColor(PineColor), maxLines = 5,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (description.isEmpty()) Text(
                        stringResource(R.string.description_hint), style = SimpleText,
                        color = PineColor.copy(0.5f)
                    )
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        val theme = getDatePickerTheme()
        TaskOption(
            text = deadline?.let {
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))
            } ?: stringResource(R.string.date_time_placeholder),
            iconRes = R.drawable.data,
            textColor = if (deadline == null) PineColor.copy(0.5f) else InverseColor
        ) {
            val calendar = Calendar.getInstance()
            deadline?.let {
                calendar.timeInMillis = it
            }

            val datePicker = android.app.DatePickerDialog(
                ContextThemeWrapper(context, theme),
                { _, year, month, day ->
                    calendar.set(year, month, day)

                    // Show time picker after date is selected
                    val timePickerDialog = android.app.TimePickerDialog(
                        ContextThemeWrapper(context, theme),
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            deadline = calendar.timeInMillis
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.proekt),
                    contentDescription = null,
                    tint = PineColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                selectedProjectId?.let { id ->
                    projects.find { it.projectId == id }?.let { project ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    Color(project.color),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Text(
                    text = selectedProjectId?.let { id ->
                        projects.find { it.projectId == id }?.title
                            ?: stringResource(R.string.select_project)
                    } ?: stringResource(R.string.project_placeholder),
                    style = H2,
                    color = if (selectedProjectId == null) PineColor.copy(0.5f) else InverseColor
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = null,
                tint = PineColor,
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }
        if (expanded) Box(modifier = Modifier.fillMaxWidth()) {
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
                                fontWeight = W500,
                                color = PineColor
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        navController.navigate(NEW_PROJECT_SCREEN)
                    }
                )
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
                                fontWeight = W500,
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
                                            Color(project.color), RoundedCornerShape(4.dp)
                                        )
                                )
                                Text(
                                    project.title, fontFamily = Montserrat, fontSize = 16.sp,
                                    fontWeight = W500, color = InverseColor
                                )
                            }
                        }, onClick = {
                            selectedProjectId = project.projectId
                            expanded = false
                        }
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = PineColor.copy(0.5f))
        TaskOption(text = stringResource(R.string.add_repeat), iconRes = R.drawable.repeat) {
            showRepeatOptions = !showRepeatOptions
            if (showRepeatOptions && repeatInterval == null) {
                repeatInterval = "0000000"
            }
        }

        if (showRepeatOptions) {
            RepeatSection(
                repeatInterval = repeatInterval ?: "0000000",
                onDaySelected = { day, selected ->
                    val days = repeatInterval?.toCharArray() ?: CharArray(7) { '0' }
                    days[day] = if (selected) '1' else '0'
                    repeatInterval = String(days)
                }
            )
        }
        TaskOption(
            text = if (estimatedDurationMs > 0) String.format(
                Locale.getDefault(),
                "%02d:%02d:00",
                estimatedDurationMs / 60,
                estimatedDurationMs % 60
            ) else stringResource(R.string.add_time_to_task),
            iconRes = R.drawable.time
        ) {
            showDurationPicker = true
        }

        if (showDurationPicker) {
            TimeInputDialog(
                initialHours = estimatedDurationMs / 60L,
                initialMinutes = estimatedDurationMs % 60L,
                onDismiss = { showDurationPicker = false },
                onTimeSet = { hours, minutes ->
                    estimatedDurationMs = hours * 60L + minutes
                    showDurationPicker = false
                }
            )
        }
    }
}

@Composable
fun RepeatSection(
    repeatInterval: String,
    onDaySelected: (Int, Boolean) -> Unit
) {
    val daysOfWeek = listOf("П", "В", "С", "Ч", "П", "С", "В")
    val selectedDays = repeatInterval.mapIndexed { index, c -> index to (c == '1') }.toMap()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEachIndexed { index, day ->
                val isSelected = selectedDays[index] == true

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PineColor else Color.Transparent)
                        .border(1.dp, PineColor, CircleShape)
                        .clickable { onDaySelected(index, !isSelected) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) Color.White else PineColor,
                        fontSize = 14.sp,
                        fontWeight = W500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Optional: Time selection for repeat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5FBE5))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "12:30",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TaskOption(text: String, iconRes: Int, textColor: Color = InverseColor, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { onClick() }
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = PineColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = text, style = H2, color = textColor)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = PineColor.copy(0.5f))
    }
}

const val NEW_TASK_SCREEN = "NewTaskScreen"

@Composable
fun getDatePickerTheme(): Int {
    return if (isDarkTheme()) R.style.CustomDatePickerDarkTheme
    else R.style.CustomDatePickerLightTheme
}

//@Composable
//fun getTimePickerTheme(): Int {
//    return if (isDarkTheme()) R.style.CustomTimePickerDarkTheme
//    else R.style.CustomTimePickerLightTheme
//}

// TimeInputDialog and OutlinedTimeField moved to separate file
