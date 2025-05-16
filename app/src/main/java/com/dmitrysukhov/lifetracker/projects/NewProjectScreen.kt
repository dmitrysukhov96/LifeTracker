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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.ColorPicker
import com.dmitrysukhov.lifetracker.common.ui.SubtitleWithIcon
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.ImageUtils
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
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

    // Initialize goal from project if available
    var goal by rememberSaveable { mutableStateOf(project?.goal ?: "") }

    // Format and initialize deadline if available
    var deadline by rememberSaveable {
        val formattedDate = project?.deadlineMillis?.let { millis ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.format(Date(millis))
        } ?: ""
        mutableStateOf(formattedDate)
    }

    setTopBarState(
        TopBarState(
            title = if (project != null) context.getString(R.string.edit)
            else context.getString(R.string.new_project), color = selectedColor,
            screen = NEW_PROJECT_SCREEN,
            imagePath = currentImagePath, topBarActions = {
                Row {
                    if (project != null) {
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
                        // Validate deadline date if not empty
                        if (deadline.isNotEmpty()) {
                            val isValidDate = try {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                dateFormat.isLenient = false
                                val parsedDate = dateFormat.parse(deadline)

                                // Check if date is before 1970
                                val calendar = Calendar.getInstance()
                                calendar.set(1970, 0, 1) // January 1, 1970
                                val minDate = calendar.time

                                if (parsedDate != null && parsedDate.before(minDate)) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_date_before_1970),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@IconButton
                                }

                                true
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_invalid_date_format),
                                    Toast.LENGTH_SHORT
                                ).show()
                                false
                            }

                            if (!isValidDate) {
                                return@IconButton
                            }
                        }

                        // Parse deadline to milliseconds if it's not empty
                        val deadlineMillis = if (deadline.isNotEmpty()) {
                            try {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                dateFormat.parse(deadline)?.time
                            } catch (_: Exception) {
                                null
                            }
                        } else {
                            null
                        }

                        // Proceed with saving if validation passes or deadline is empty
                        if (project != null) viewModel.updateProject(
                            project.copy(
                                title = title,
                                description = descr,
                                color = selectedColorInt,
                                imagePath = currentImagePath,
                                goal = goal.takeIf { it.isNotEmpty() },
                                deadlineMillis = deadlineMillis
                            )
                        ) else viewModel.addProject(
                            Project(
                                title = title,
                                description = descr,
                                color = selectedColorInt,
                                imagePath = currentImagePath,
                                goal = goal.takeIf { it.isNotEmpty() },
                                deadlineMillis = deadlineMillis
                            )
                        )
                        navController.navigateUp()
                    }) {
                        if (title.isNotBlank()) Icon(
                            painter = painterResource(R.drawable.tick),
                            contentDescription = null, tint = Color.White
                        )
                    }
                }
            }
        )
    )
    Column(
        Modifier
            .background(if (isDarkTheme()) Color.Black else Color.White)
            .background(selectedColor.copy(alpha = if (isDarkTheme()) 0.1f else 0.05f))
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
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = selectedColor.copy(0.5f), thickness = 0.5.dp)
        BasicTextField(
            value = descr,
            onValueChange = { if (it.length <= 300) descr = it },
            textStyle = SimpleText.copy(color = InverseColor),
            cursorBrush = SolidColor(selectedColor),
            maxLines = 5,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (descr.isEmpty()) Text(
                        stringResource(R.string.description_hint), style = SimpleText,
                        color = selectedColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        SubtitleWithIcon(
            textRes = R.string.select_color, iconRes = R.drawable.palette, iconColor = selectedColor
        )
        ColorPicker(
            selectedColorInt = selectedColorInt, onColorSelected = { selectedColorInt = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = selectedColor.copy(0.5f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { imagePickerLauncher.launch("image/*") },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SubtitleWithIcon(
                textRes = R.string.add_photo,
                iconRes = R.drawable.image_add,
                iconColor = selectedColor
            )
            if (currentImagePath == null) Icon(
                painterResource(R.drawable.plus),
                null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(16.dp),
                tint = selectedColor
            )
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
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = selectedColor.copy(0.5f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))
        SubtitleWithIcon(
            textRes = R.string.goal_colon,
            iconRes = R.drawable.goal,
            iconColor = selectedColor
        )
        BasicTextField(
            value = goal, cursorBrush = SolidColor(selectedColor),
            onValueChange = { if (it.length < 200) goal = it }, maxLines = 3,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp)
                ) {
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
        val datePickerTheme = getDatePickerTheme()
        val showDatePicker = {
            val calendar = Calendar.getInstance()
            if (deadline.isNotEmpty()) {
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.parse(deadline)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (_: Exception) {
                }
            }

            val datePicker = android.app.DatePickerDialog(
                ContextThemeWrapper(context, datePickerTheme),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    calendar.set(Calendar.HOUR_OF_DAY, 12)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    deadline = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { showDatePicker() }, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SubtitleWithIcon(
                textRes = R.string.add_deadline,
                iconRes = R.drawable.data,
                iconColor = selectedColor
            )
            if (deadline.isEmpty()) Icon(
                painterResource(R.drawable.plus), null, modifier = Modifier
                    .padding(end = 4.dp)
                    .size(16.dp), tint = selectedColor
            )
        }
        Row(modifier = Modifier.padding(start = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (deadline.isEmpty())
                    stringResource(R.string.deadline)
                else deadline,
                style = H2,
                color = if (deadline.isEmpty()) selectedColor.copy(0.5f) else InverseColor
            )

            if (deadline.isNotEmpty()) {
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = { deadline = "" },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = InverseColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

        }
        HorizontalDivider(thickness = 0.5.dp, color = selectedColor.copy(0.5f))
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

@Composable
fun getDatePickerTheme(): Int {
    return if (isDarkTheme()) R.style.CustomDatePickerDarkTheme
    else R.style.CustomDatePickerLightTheme
}