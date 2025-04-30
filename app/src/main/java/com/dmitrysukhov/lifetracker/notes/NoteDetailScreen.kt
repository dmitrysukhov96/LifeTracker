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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine

@Composable
fun NoteDetailScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val selectedNote by viewModel.selectedNote.collectAsState()
    val projects by viewModel.projects.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    // Load note data into state
    LaunchedEffect(selectedNote) {
        selectedNote?.let {
            title = it.title
            content = it.content
            selectedProjectId = it.projectId
        }
    }
    
    // Set top bar state with editing actions
    setTopBarState(
        TopBarState(
            title = if (isEditing) stringResource(R.string.edit_note) else stringResource(R.string.note),
            color = PineColor,
            topBarActions = {
                if (isEditing) {
                    // Save button when editing
                    IconButton(onClick = {
                        selectedNote?.let {
                            viewModel.updateNote(it.copy(
                                title = title, 
                                content = content,
                                projectId = selectedProjectId
                            ))
                            isEditing = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.save_note),
                            tint = WhitePine
                        )
                    }
                } else {
                    // Edit button when viewing
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_note),
                            tint = WhitePine
                        )
                    }
                    
                    // Delete button
                    IconButton(onClick = {
                        selectedNote?.let {
                            viewModel.deleteNote(it)
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_note),
                            tint = WhitePine
                        )
                    }
                }
            }
        )
    )
    
    // If selectedNote is null but we're on this screen, it means that
    // viewModel instance might be new and hasn't loaded the selected note
    LaunchedEffect(Unit) {
        if (selectedNote == null) {
            // Return to notes list
            navController.popBackStack()
        }
    }
    
    selectedNote?.let { note ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(16.dp)
        ) {
            if (isEditing) {
                // Edit mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.note_title)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            focusedLabelColor = PineColor,
                            cursorColor = PineColor
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Project Selector
                    Text(
                        text = stringResource(R.string.select_project),
                        fontFamily = Montserrat,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .clickable { expanded = true }
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(
                                    if (expanded) 2.dp else 1.dp,
                                    if (expanded) PineColor else InverseColor.copy(0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 16.dp),
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
                                            } else {
                                                modifier
                                            }
                                        }
                                )
                                Text(
                                    text = selectedProjectId?.let { id ->
                                        projects.find { it.projectId == id }?.title
                                            ?: stringResource(R.string.select_project)
                                    } ?: stringResource(R.string.no_project),
                                    fontFamily = Montserrat,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InverseColor
                                )
                            }
                            Icon(
                                painterResource(R.drawable.arrow_down),
                                contentDescription = null,
                                tint = PineColor,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .rotate(if (expanded) 180f else 0f)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(BgColor)
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
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
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
                            
                            // No project option
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
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
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
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
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
                            .weight(1f),
                        label = { Text(stringResource(R.string.note_content)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PineColor,
                            focusedLabelColor = PineColor,
                            cursorColor = PineColor
                        )
                    )
                }
            } else {
                // View mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = note.title,
                        fontFamily = Montserrat,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    
                    // Project tag if exists
                    note.projectId?.let { projectId ->
                        val project = projects.find { it.projectId == projectId }
                        project?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(it.color), RoundedCornerShape(2.dp))
                                )
                                Text(
                                    text = it.title,
                                    fontFamily = Montserrat,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = note.content,
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    } ?: run {
        // Fallback if note is null
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.note_not_found),
                fontFamily = Montserrat,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
} 