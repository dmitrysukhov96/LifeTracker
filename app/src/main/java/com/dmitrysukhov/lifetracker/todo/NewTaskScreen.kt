package com.dmitrysukhov.lifetracker.todo

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
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
        mutableStateOf(
            viewModel.selectedTask?.description ?: ""
        )
    }
    var selectedProjectId by rememberSaveable {
        mutableStateOf(viewModel.selectedTask?.projectId)
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var deadline by rememberSaveable {
        mutableStateOf(viewModel.selectedTask?.dateTime)
    }
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()

    // Determine if we're editing an existing task
    val isEditing = viewModel.selectedTask != null

    val globalTextStyle = TextStyle(
        fontFamily = FontFamily(Font(R.font.montserrat_regular)), fontSize = 16.sp,
        color = Color.Black
    )

    val topBarTitle =
        if (isEditing) stringResource(R.string.edit_task) else stringResource(R.string.new_task)
    val saveToastText = if (isEditing) stringResource(R.string.update_task_toast) else stringResource(R.string.save_task_toast)

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = topBarTitle,
                topBarActions = {
                    Row {
                        // Show delete button only when editing
                        if (isEditing) {
                            IconButton({
                                viewModel.selectedTask?.let { task ->
                                    viewModel.deleteTask(task)
                                    Toast.makeText(context, context.getString(R.string.task_deleted), Toast.LENGTH_SHORT).show()
                                    navController.navigateUp()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.delete),
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color.White,
                                )
                            }
                        }
                        
                        // Save/Update button
                        if (title.isNotEmpty()) {
                            IconButton({
                                if (isEditing) {
                                    // Update existing task
                                    viewModel.selectedTask?.let { task ->
                                        viewModel.updateTask(
                                            task.copy(
                                                text = title,
                                                description = description,
                                                projectId = selectedProjectId,
                                                dateTime = deadline
                                            )
                                        )
                                    }
                                } else {
                                    // Create new task
                                    viewModel.addTask(
                                        text = title,
                                        description = description,
                                        projectId = selectedProjectId,
                                        deadline = deadline
                                    )
                                }
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

    CompositionLocalProvider(LocalTextStyle provides globalTextStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            BasicTextField(
                value = title, onValueChange = { title = it },
                cursorBrush = SolidColor(PineColor),
                textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) Text(
                            stringResource(R.string.title_hint),
                            fontSize = 18.sp,
                            fontWeight = W700,
                            fontFamily = Montserrat,
                            color = PineColor.copy(0.5f)
                        )
                        innerTextField()
                    }
                }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

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
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            // Deadline selection
            TaskOption(
                text = deadline?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                } ?: stringResource(R.string.no_deadline),
                iconRes = R.drawable.data
            ) {
                val calendar = Calendar.getInstance()
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selectedDate = Calendar.getInstance().apply {
                            set(year, month, day)
                        }
                        deadline = selectedDate.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }
            HorizontalDivider()

            // Project selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
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
                            fontWeight = W500,
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
                                                Color(project.color),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Text(
                                        project.title,
                                        fontFamily = Montserrat,
                                        fontSize = 16.sp,
                                        fontWeight = W500,
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
            HorizontalDivider()

        }
    }
}

@Composable
fun TaskOption(text: String, iconRes: Int, showIcon: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = PineColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = H2, color = InverseColor)
        }
        if (showIcon) Icon(
            painter = painterResource(id = R.drawable.arrow_down),
            contentDescription = null, tint = PineColor
        )
    }
}

const val NEW_TASK_SCREEN = "NewTaskScreen"
