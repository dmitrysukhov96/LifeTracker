package com.dmitrysukhov.lifetracker.projects

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.ColorPicker
import com.dmitrysukhov.lifetracker.common.ui.SubtitleWithIcon
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.ImageUtils
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.isDarkTheme
import java.io.File

@Composable
fun NewProjectScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val project = viewModel.selectedProject
    var title by rememberSaveable { mutableStateOf(project?.title ?: "") }
    var descr by rememberSaveable { mutableStateOf(project?.description ?: "") }
    var currentImagePath by rememberSaveable { mutableStateOf(project?.imagePath) }
    var selectedColorInt by rememberSaveable {
        mutableIntStateOf(project?.color ?: PineColor.toArgb())
    }
    var selectedColor = Color(selectedColorInt)
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = if (project != null) context.getString(R.string.edit)
                else context.getString(R.string.new_project), color = selectedColor,
                screen = NEW_PROJECT_SCREEN,
                imagePath = currentImagePath
            )
        )
    }
    Column(
        Modifier
            .background(if (isDarkTheme()) Color.Black else Color.White)
            .background(selectedColor.copy(alpha = 0.1f))
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        BasicTextField(
            value = title, onValueChange = { if (it.length <= 90) title = it },
            textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (title.isEmpty())
                        Text(
                            stringResource(R.string.project_title), style = H1,
                            color = selectedColor.copy(0.5f)
                        )
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth(), maxLines = 1,
            cursorBrush = SolidColor(selectedColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        BasicTextField(
            value = descr, cursorBrush = SolidColor(selectedColor),
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

        // Color selection FIRST
        SubtitleWithIcon(
            textRes = R.string.select_color,
            iconRes = R.drawable.palette,
            iconColor = selectedColor
        )
        ColorPicker(
            selectedColorInt = selectedColorInt,
            onColorSelected = { selectedColorInt = it }
        )
        Spacer(modifier = Modifier.height(24.dp))
        SubtitleWithIcon(
            textRes = R.string.add_photo,
            iconRes = R.drawable.image_add,
            iconColor = selectedColor
        )
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                val newPath = ImageUtils.saveImageToInternalStorage(context, it)
                project?.imagePath?.let { oldPath ->
                    ImageUtils.deleteImageFromInternalStorage(context, oldPath)
                }
                currentImagePath = newPath
            }
        }
        if (currentImagePath?.isNotEmpty() == true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(File(context.filesDir, currentImagePath)),
                    contentDescription = stringResource(R.string.selected_image),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.change_image),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            project?.imagePath?.let { oldPath ->
                                ImageUtils.deleteImageFromInternalStorage(context, oldPath)
                            }
                            currentImagePath = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = stringResource(R.string.delete_image),
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(selectedColor.copy(alpha = 0.2f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.import_icon),
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.add_photo),
                        color = selectedColor,
                        style = TextStyle(fontSize = 16.sp, fontWeight = W700)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Goal field
        SubtitleWithIcon(
            textRes = R.string.goal_colon,
            iconRes = R.drawable.task,
            iconColor = selectedColor
        )
        var goal by rememberSaveable { mutableStateOf("") }
        BasicTextField(
            value = goal,
            onValueChange = { goal = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (goal.isEmpty()) Text(
                        stringResource(R.string.goal_colon),
                        style = H2, color = selectedColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Deadline field
        SubtitleWithIcon(
            textRes = R.string.add_deadline,
            iconRes = R.drawable.data,
            iconColor = selectedColor
        )
        var deadline by rememberSaveable { mutableStateOf("") }
        BasicTextField(
            value = deadline,
            onValueChange = { deadline = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (deadline.isEmpty()) Text(
                        stringResource(R.string.add_deadline),
                        style = H2, color = selectedColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_project), style = H1) },
            text = { Text(stringResource(R.string.delete_project_confirmation), style = H2) },
            confirmButton = {
                TextButton(onClick = {
                    project?.let { viewModel.deleteProject(it.projectId) }
                    showDeleteConfirmation = false
                    viewModel.selectedProject = null
                    navController.popBackStack(PROJECTS_SCREEN, false, false)
                }) { Text(stringResource(R.string.delete), style = H2) }
            }, dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel), style = H2)
                }
            }
        )
    }
}

const val NEW_PROJECT_SCREEN = "new_project_screen"