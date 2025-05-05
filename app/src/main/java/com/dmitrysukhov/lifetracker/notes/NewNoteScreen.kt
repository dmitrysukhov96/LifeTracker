package com.dmitrysukhov.lifetracker.notes

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dmitrysukhov.lifetracker.Note
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine

@Composable
fun NewNoteScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(viewModel.selectedNote.value != null) }
    var selectedNote = viewModel.selectedNote
    LaunchedEffect(selectedNote) {
        viewModel.selectedNote.value?.let { note ->
            title = note.title
            content = note.content
            selectedProjectId = note.projectId
        }
    }
    val projects by viewModel.projects.collectAsState()
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = context.getString(R.string.new_note), color = PineColor, topBarActions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (isEditing) {
                                    viewModel.updateNote(
                                        selectedNote.value?.copy(
                                            title = title,
                                            content = content,
                                            projectId = selectedProjectId
                                        )
                                            ?: Note(
                                                title = title,
                                                content = content,
                                                projectId = selectedProjectId
                                            )
                                    )
                                } else {
                                    viewModel.createNote(title, content, selectedProjectId)
                                }
                                navController.popBackStack()
                            }
                        }, enabled = title.isNotBlank()
                    ) {
                        Icon(
                            painterResource(R.drawable.tick),
                            contentDescription = stringResource(R.string.save_note),
                            tint = WhitePine
                        )
                    }
                }
            )
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BasicTextField(
                value = title, onValueChange = { title = it }, cursorBrush = SolidColor(PineColor),
                textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) Text(
                            stringResource(R.string.title_hint),
                            style = H1.copy(color = PineColor.copy(0.5f))
                        )
                        innerTextField()
                    }
                }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .fillMaxWidth()
                        .height(48.dp),
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
                                    } else modifier
                                }
                        )
                        Text(
                            text = selectedProjectId?.let { id ->
                                projects.find { it.projectId == id }?.title
                                    ?: stringResource(R.string.select_project)
                            } ?: stringResource(R.string.no_project),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = InverseColor
                        )
                    }
                    Icon(
                        painterResource(R.drawable.arrow_down),
                        contentDescription = null,
                        tint = PineColor,
                        modifier = Modifier
                            .padding(top = 1.dp, end = 8.dp)
                            .rotate(if (expanded) 180f else 0f)
                    )
                }

                DropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(BgColor)
                        .border(1.dp, PineColor, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
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
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(300.dp),
                label = {
                    Text(
                        stringResource(R.string.note_content),
                        style = H2.copy(fontWeight = FontWeight.Bold), fontSize = 18.sp
                    )
                },
                placeholder = {
                    Text(
                        stringResource(R.string.enter_note_content),
                        style = H2.copy(
                            color = InverseColor.copy(0.7f)
                        )
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PineColor,
                    focusedLabelColor = PineColor,
                    cursorColor = PineColor, unfocusedLabelColor = PineColor.copy(0.5f),
                ),
                textStyle = H2.copy(color = InverseColor)
            )
        }
    }
}