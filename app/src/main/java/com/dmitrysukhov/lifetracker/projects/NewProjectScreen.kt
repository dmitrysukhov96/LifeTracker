package com.dmitrysukhov.lifetracker.projects

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NewProjectScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val colors = listOf(
        Color(0xFFFA3535),
        Color(0xFFFF582E),
        Color(0xFFFFA91F),
        Color(0xFFFFE030),
        Color(0xFFDBE204),
        Color(0xFFC1FF4D),
        Color(0xFF8FFF2E),
        Color(0xFF84E09E),
        Color(0xFF39E25D),
        Color(0xFF14C56D),
        Color(0xFF0ECC8A),
        Color(0xFF29B8D9),
        Color(0xFF669DE5),
        Color(0xFF737AFF),
        Color(0xFF7940FF),
        Color(0xFF983DC2),
        Color(0xFFC02A39),
        Color(0xFFED1F60),
        Color(0xFFE056CE),
        Color(0xFFF87687)
    )
    
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedColorIndex by rememberSaveable { mutableStateOf(0) }
    val selectedColor = colors[selectedColorIndex]
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val project = viewModel.selectedProject
    val isEditMode = project != null
//    val context = LocalContext.current
    
    // If in edit mode, load project data
    LaunchedEffect(project) {
        project?.let {
            title = it.title
            description = it.description
            // Find matching color in our palette or default to first color
            val projectColor = Color(it.color)
            val closestColorIndex = findClosestColorIndex(projectColor, colors)
            selectedColorIndex = closestColorIndex
        }
    }
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(if (isEditMode) "Edit Project" else "New Project") {
            Row {
                if (isEditMode) {
                    IconButton(onClick = {
                        showDeleteConfirmation = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = {
                    if (isEditMode) {
                        project?.let {
                            viewModel.updateProject(
                                it.copy(
                                    title = title,
                                    description = description,
                                    color = colors[selectedColorIndex].toArgb()
                                )
                            )
                        }
                    } else {
                        viewModel.addProject(
                            Project(
                                title = title,
                                description = description,
                                color = colors[selectedColorIndex].toArgb()
                            )
                        )
                    }
                    navController.navigateUp()
                }) {
                    if (title.isNotBlank()) Icon(
                        painter = painterResource(R.drawable.tick),
                        contentDescription = null, tint = Color.White
                    )
                }
            }
        })
    }
    
    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        BasicTextField(
            value = title, onValueChange = { title = it },
            textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty())
                        Text("Заголовок", style = H1, color = PineColor.copy(0.5f))
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        BasicTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (description.isEmpty()) Text(
                        stringResource(R.string.description_hint),
                        style = H2, color = PineColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Column(Modifier.padding(top = 32.dp)) {
            Row (Modifier.padding(bottom = 10.dp)){
                Image(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = "",
                    Modifier.padding(end = 8.dp)
                )
                Text(text = "Выберите цвет проекта", style = H2)
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0 until 10) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors[i])
                                    .clickable { selectedColorIndex = i }
                            ) {
                                if (selectedColor == colors[i]) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.padding(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 10 until 20) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors[i])
                                    .clickable { selectedColorIndex = i }
                            ) {
                                if (selectedColor == colors[i]) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_project)) },
            text = { Text(stringResource(R.string.delete_project_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    project?.let { viewModel.deleteProject(it.projectId) }
                    showDeleteConfirmation = false
                    viewModel.selectedProject = null
                    navController.popBackStack(PROJECTS_SCREEN, false, false)
                }) { Text(stringResource(R.string.delete)) }
            }, dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// Helper function to find the closest matching color
fun findClosestColorIndex(targetColor: Color, colorList: List<Color>): Int {
    var closestIndex = 0
    var minDistance = Float.MAX_VALUE
    
    colorList.forEachIndexed { index, color ->
        val distance = colorDistance(targetColor, color)
        if (distance < minDistance) {
            minDistance = distance
            closestIndex = index
        }
    }
    
    return closestIndex
}

// Calculate "distance" between two colors
fun colorDistance(c1: Color, c2: Color): Float {
    val rDiff = c1.red - c2.red
    val gDiff = c1.green - c2.green
    val bDiff = c1.blue - c2.blue
    return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff
}

const val NEW_PROJECT_SCREEN = "new_project_screen"