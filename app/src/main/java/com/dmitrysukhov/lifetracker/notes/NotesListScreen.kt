package com.dmitrysukhov.lifetracker.notes

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dmitrysukhov.lifetracker.Note
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NotesListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    // Set top bar state with Add button
    setTopBarState(
        TopBarState(
            title = stringResource(R.string.notes), color = PineColor, topBarActions = {
                IconButton(onClick = { navController.navigate(NEW_NOTE_SCREEN) }) {
                    Icon(
                        painter = painterResource(R.drawable.plus), contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        )
    )
    
    val notes by viewModel.notes.collectAsState()
    val projects by viewModel.projects.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        if (notes.isEmpty()) {
            // Empty state using EmptyPlaceholder component
            EmptyPlaceholder(R.string.no_notes_yet, R.string.tap_plus_to_create_note)
        } else {
            // Notes list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    NoteItem(
                        note = note, 
                        onNoteClick = {
                            viewModel.selectNote(note)
                            navController.navigate(NOTE_DETAIL_SCREEN)
                        },
                        project = projects.find { it.projectId == note.projectId }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun NoteItem(note: Note, onNoteClick: () -> Unit, project: Project? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.title,
                    style = H2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Project tag if exists
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
                        style = SimpleText.copy(
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = note.content,
                style = SimpleText,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Last updated time
            Text(
                text = getFormattedTime(note.updatedAt),
                style = SimpleText.copy(
                    fontSize = 12.sp,
                    color = Color.Gray
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun getFormattedTime(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
} 