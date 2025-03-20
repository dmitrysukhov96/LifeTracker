package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ProjectsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        items(projects) { project ->
            Item(
                title = project.title,
                progress = "${project.completed}/${project.total} выполнено",
                deadline = "до ${project.deadline}",
                color = project.color
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun Item(title: String, progress: String, deadline: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(text = progress, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = deadline, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}


val projects = listOf(
    Project("Лайф", 4, 10, "25.10.2025", Color(0xFF3366FF)),
    Project("Покупки", 2, 9, "12.03.2025", Color(0xFFFF9800)),
    Project("Работа", 0, 100, "23.02.2025", Color(0xFF757575)),
    Project("Спорт", 10, 10, "12.03.2025", Color(0xFF009688)),
    Project("Дизайн LifeTracker", 6, 13, "15.01.2025", Color(0xFF4CAF50)),
    Project("Подготовиться к молодёжи", 2, 3, "10.01.2025", Color(0xFF795548)),
    Project("Лайф", 5, 23, "04.01.2025", Color(0xFF673AB7))
)

data class Project(val title: String, val completed: Int, val total: Int, val deadline: String, val color: Color)


const val PROJECTS_SCREEN = "Projects"