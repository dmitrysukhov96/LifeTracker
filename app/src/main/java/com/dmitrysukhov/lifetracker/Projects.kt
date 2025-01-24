package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class Project(
    val title: String,
    val progress: String,
    val dueDate: String,
    val color: Color,
    val imageRes: Int? = null
)

val projects = listOf(
    Project("Лайф", "4/10 выполнено", "до 25.10.2025", Color(0xFF2196F3), R.drawable.project_life),
    Project("Покупки", "2/9 выполнено", "до 12.03.2025", Color(0xFFFF9800)),
    Project("Работа", "0/100 выполнено", "до 23.02.2025", Color(0xFF4CAF50), R.drawable.ic_work),
    Project("Спорт", "10/10 выполнено", "до 12.03.2025", Color(0xFFF44336), R.drawable.ic_sport),
    Project("Дизайн LifeTracker", "6/13 выполнено", "до 15.01.2025", Color(0xFF2AC17C))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                actions = {
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Handle new project */ }) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add Project")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(projects) { project ->
                ProjectCard(project)
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = project.color)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            project.imageRes?.let { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = project.progress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = project.dueDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}