package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
import com.dmitrysukhov.lifetracker.common.ui.ColorPicker
import com.dmitrysukhov.lifetracker.common.ui.SubtitleWithIcon
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NewProjectScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val project = viewModel.selectedProject
    var title by rememberSaveable { mutableStateOf(project?.title ?: "") }
    var descr by rememberSaveable { mutableStateOf(project?.description ?: "") }
    var selectedColorInt by rememberSaveable {
        mutableIntStateOf(
            project?.color ?: PineColor.toArgb()
        )
    }
    var selectedColor = Color(selectedColorInt)
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val isEditMode = project != null
    LaunchedEffect(selectedColor) {
        setTopBarState(
            TopBarState(
                title = if (isEditMode) context.getString(R.string.edit_project) else context.getString(
                    R.string.new_project
                ), color = selectedColor
            ) {
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
                        if (isEditMode && project != null) viewModel.updateProject(
                            project.copy(
                                title = title, description = descr, color = selectedColorInt
                            )
                        ) else viewModel.addProject(
                            Project(title = title, description = descr, color = selectedColorInt)
                        )
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
                        Text(
                            stringResource(R.string.project_title),
                            style = H1,
                            color = selectedColor.copy(0.5f)
                        )
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        BasicTextField(
            value = descr,
            onValueChange = { descr = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (descr.isEmpty()) Text(
                        stringResource(R.string.description_hint),
                        style = H2, color = selectedColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Column(Modifier.padding(top = 32.dp)) {
            SubtitleWithIcon(
                textRes = R.string.select_color, iconRes = R.drawable.palette, iconColor = selectedColor
            )
            ColorPicker(
                selectedColorInt = selectedColorInt,
                onColorSelected = { selectedColorInt = it }
            )
        }
    }
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

const val NEW_PROJECT_SCREEN = "new_project_screen"