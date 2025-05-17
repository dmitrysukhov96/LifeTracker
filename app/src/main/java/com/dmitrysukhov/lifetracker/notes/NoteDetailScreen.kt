package com.dmitrysukhov.lifetracker.notes

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine

@Composable
fun NoteDetailScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedNote by viewModel.selectedNote.collectAsState()
    val projects by viewModel.projects.collectAsState()

    setTopBarState(
        TopBarState(
            title = context.getString(R.string.note), color = PineColor,
            screen = NOTE_DETAIL_SCREEN, topBarActions = {
                IconButton(onClick = { navController.navigate(NEW_NOTE_SCREEN) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.save_note),
                        tint = WhitePine
                    )
                }
                IconButton(onClick = {
                    selectedNote?.let {
                        viewModel.deleteNote(it)
                        navController.navigateUp()
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = stringResource(R.string.delete_note),
                        tint = WhitePine
                    )
                }
            }
        )
    )
    selectedNote?.let { note ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = note.title,
                    style = H1.copy(color = InverseColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
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
                            Text(text = it.title, style = Small.copy(color = InverseColor))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = note.content, style = H2.copy(color = InverseColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}