package com.dmitrysukhov.lifetracker.notes

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dmitrysukhov.lifetracker.Note
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NotesListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavController,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    setTopBarState(
        TopBarState(
            title = context.getString(R.string.notes),
            color = PineColor, screen = NOTES_SCREEN, topBarActions = {
                IconButton(onClick = {
                    viewModel.selectNote(null)
                    navController.navigate(NEW_NOTE_SCREEN)
                }) {
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
            EmptyPlaceholder(R.string.no_notes_yet, R.string.tap_plus_to_create_note)
        } else {
            // Notes list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
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
    val color = project?.color?.let { Color(it) } ?: PineColor
    Column(
        modifier = Modifier
            .background(color.copy(0.2f), RoundedCornerShape(20.dp))
            .border(
                1.dp, color, RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .clickable(onClick = onNoteClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = note.title,
                style = H2.copy(color = InverseColor),
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
                Text(text = it.title, style = Small.copy(color = InverseColor))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note.content, style = SimpleText, color = InverseColor,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getFormattedTime(note.updatedAt),
            style = Small.copy(color = InverseColor),
            modifier = Modifier.align(Alignment.End)
        )
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