package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun ProjectsScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
) {
    val viewModel: ProjectsViewModel = hiltViewModel()
    val projects by remember { derivedStateOf { viewModel.projects } }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState("Projects") {
                IconButton(onClick = { navController.navigate(NEW_PROJECT_SCREEN) }) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null, tint = Color.White
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 24.dp)
    ) {
        item { Spacer(Modifier.height(24.dp)) }

        items(projects) { project ->
            val deadlineText = project.deadlineMillis?.let {
                val date = Date(it)
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
            } ?: "Без дедлайна"

            ProjectItem(
                title = project.title,
                progress = "${project.completedTasks}/${project.totalTasks} выполнено",
                deadline = deadlineText, showImage = project == projects[0],
                gradient = generateGradient(Color(project.color)), onClick = {
                    // navController.navigate(...) если нужно
                })

            Spacer(modifier = Modifier.height(16.dp))
        }

        item { Spacer(Modifier.height(64.dp)) }
    }
}

fun generateRandomColor(): Int {
    val hue = Random.nextFloat() * 360f
    val saturation = 0.7f + Random.nextFloat() * 0.3f
    val value = 0.7f + Random.nextFloat() * 0.3f
    val hsv = floatArrayOf(hue, saturation, value)
    return android.graphics.Color.HSVToColor(hsv)
}

@Composable
fun ProjectItem(
    title: String, progress: String, deadline: String, gradient: Brush, onClick: () -> Unit,
    showImage: Boolean
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .then (
                if (showImage) Modifier.background(Color.Black) else Modifier.background(gradient)
                )
    ) {
        if (showImage) Image(
            painter = painterResource(R.drawable.egg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.9f),
            contentScale = ContentScale.Crop
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp)
        )
        Text(
            text = progress,
            fontSize = 12.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 8.dp)
        )
        Text(
            text = deadline,
            fontSize = 12.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
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