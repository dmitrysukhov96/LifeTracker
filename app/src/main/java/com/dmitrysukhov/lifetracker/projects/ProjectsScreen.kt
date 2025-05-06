package com.dmitrysukhov.lifetracker.projects

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    todoViewModel: TodoViewModel, viewModel: ProjectsViewModel
) {
    val context = LocalContext.current
    val projects by remember { derivedStateOf { viewModel.projects } }
    setTopBarState(
        TopBarState(context.getString(R.string.projects)) {
            IconButton(onClick = {
                viewModel.selectedProject = null
                navController.navigate(NEW_PROJECT_SCREEN)
            }) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = null, tint = Color.White
                )
            }
        }
    )
    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
    ) {
        if (projects.isEmpty()) EmptyPlaceholder(R.string.no_projects, R.string.create_project_hint)
        else LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(horizontal = 24.dp)
        ) {
            item { Spacer(Modifier.height(24.dp)) }
            items(projects.size) { index ->
                val project = projects[index]
                val deadlineText = project.deadlineMillis?.let {
                    val date = Date(it)
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
                } ?: stringResource(R.string.no_deadline)
                val tasks = todoViewModel.todoList.collectAsStateWithLifecycle(emptyList()).value
                val projectTasks = tasks.filter { it.projectId == project.projectId }
                val completedTasks = projectTasks.count { it.isDone }
                val totalTasks = projectTasks.size
                val progressText = "$completedTasks/$totalTasks completed"
                ProjectItem(
                    title = project.title,
                    progress = progressText,
                    deadline = deadlineText,
                    gradient = generateGradient(Color(project.color)),
                    onClick = {
                        viewModel.selectedProject = project
                        navController.navigate(VIEW_PROJECT_SCREEN)
                    },
                    context = context,
                    imagePath = project.imagePath
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item { Spacer(Modifier.height(64.dp)) }
        }
    }
}

@Composable
fun ProjectItem(
    title: String, progress: String, deadline: String, gradient: Brush, onClick: () -> Unit,
    context: Context, imagePath: String?
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
    ) {
        if (!imagePath.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Image(
                painter = rememberAsyncImagePainter(File(context.filesDir, imagePath)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = title, style = H2, color = Color.White, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp), maxLines = 2
        )
        Text(
            text = progress,
            style = SimpleText,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 8.dp)
        )
        Text(
            text = deadline,
            style = SimpleText,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 8.dp)
        )
    }
}

fun generateGradient(baseColor: Color): Brush {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (baseColor.red * 255).toInt(),
        (baseColor.green * 255).toInt(),
        (baseColor.blue * 255).toInt(),
        hsv
    )
    hsv[2] *= 0.7f
    return Brush.verticalGradient(
        colors = listOf(baseColor, Color(android.graphics.Color.HSVToColor(hsv)))
    )
}

const val PROJECTS_SCREEN = "projects_screen"