package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.todo.NEW_TASK_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.utils.BoldText
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.isDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ViewProjectScreen(
    setTopBarState: (TopBarState) -> Unit, viewModel: ProjectsViewModel,
    navController: NavHostController, todoViewModel: TodoViewModel = hiltViewModel()
) {
    val project = viewModel.selectedProject
    val projectColor = project?.let { Color(it.color) } ?: Color(0xFF669DE5)
    setTopBarState(
        TopBarState(
            title = project?.title ?: "", color = projectColor, screen = VIEW_PROJECT_SCREEN,
            imagePath = if (project?.imagePath.isNullOrEmpty()) null else project.imagePath,
            topBarActions = {
                IconButton(onClick = {
                    // Keep the selected project and navigate to edit screen
                    navController.navigate(NEW_PROJECT_SCREEN)
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White)
                }
            }
        )
    )
    Column(
        modifier = Modifier
            .background(if (isDarkTheme()) Color.Black else Color.White)
            .background(projectColor.copy(alpha = if (isDarkTheme()) 0.1f else 0.05f))
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Only show description if it's not empty
        if (!project?.description.isNullOrEmpty()) {
            Text(stringResource(R.string.description_colon), style = BoldText, color = projectColor)
            Text(
                project.description,
                style = SimpleText,
                color = InverseColor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!project?.goal.isNullOrEmpty()) {
            Text(stringResource(R.string.goal_colon), style = BoldText, color = projectColor)
            Text(project.goal, style = SimpleText, color = InverseColor)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (project?.deadlineMillis != null) {
            Text(stringResource(R.string.deadline), style = BoldText, color = projectColor)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text(
                dateFormat.format(Date(project.deadlineMillis)),
                style = SimpleText,
                color = InverseColor
            )
        }
        if (!project?.description.isNullOrEmpty() || !project?.goal.isNullOrEmpty() || project?.deadlineMillis != null) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        HorizontalDivider(color = projectColor.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.task),
                contentDescription = null, tint = projectColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.tasks_colon), style = H2, color = InverseColor)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier.size(16.dp),
                onClick = {
                    todoViewModel.selectedTask = null
                    navController.navigate(NEW_TASK_SCREEN)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = stringResource(R.string.add_task),
                    tint = projectColor
                )
            }
            Spacer(Modifier.width(20.dp))
        }

        val allTasks = todoViewModel.todoList.collectAsState(initial = emptyList()).value
        val projectTasks = allTasks.filter { it.projectId == project?.projectId }

        if (projectTasks.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_tasks), style = H2,
                    color = PineColor, modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.add_task_hint), style = Small,
                    color = InverseColor.copy(alpha = 0.7f)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(projectTasks) { task ->
                    ProjectTaskItem(
                        task = task,
                        projectColor = projectColor,
                        onCheckedChange = { isChecked ->
                            val now = System.currentTimeMillis()
                            val updatedTask = if (isChecked) {
                                task.copy(isDone = true, completeDate = now)
                            } else {
                                task.copy(isDone = false, completeDate = null)
                            }
                            todoViewModel.updateTask(updatedTask)
                        },
                        onTaskClick = {
                            todoViewModel.selectedTask = task
                            navController.navigate(NEW_TASK_SCREEN)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = projectColor.copy(alpha = 0.5f))
    }
}

@Composable
fun ProjectTaskItem(
    task: TodoItem, projectColor: Color, onCheckedChange: (Boolean) -> Unit,
    onTaskClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                if (task.isDone) R.drawable.checked else R.drawable.not_checked
            ),
            contentDescription = null,
            tint = if (task.isDone) Color.Unspecified else InverseColor,
            modifier = Modifier
                .size(20.dp)
                .clickable { onCheckedChange(!task.isDone) }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = task.text,
            style = SimpleText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (task.isDone) projectColor.copy(alpha = 0.7f) else InverseColor,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )

        // Due date if available
        task.dateTime?.let { timestamp ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(timestamp)),
                style = SimpleText.copy(fontSize = 12.sp),
                color = projectColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

const val VIEW_PROJECT_SCREEN = "view_project_screen"