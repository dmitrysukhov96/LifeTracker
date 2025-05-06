package com.dmitrysukhov.lifetracker.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TimeTracker(
    lastEvent: Event?, projects: List<Project>, onActionClick: () -> Unit,
    onCircleButtonClick: () -> Unit, modifier: Modifier = Modifier
) {
    var timeElapsed by remember { mutableLongStateOf(0L) }
    val backgroundColor by animateColorAsState(
        targetValue = if (lastEvent != null && lastEvent.endTime == null) AccentColor else Color(0xFFC2EBD6),
        animationSpec = tween(durationMillis = 300), label = "trackerColor"
    )
    LaunchedEffect(lastEvent) {
        while (true) {
            timeElapsed = when {
                lastEvent == null -> 0L
                lastEvent.endTime == null -> (System.currentTimeMillis() - lastEvent.startTime) / 1000
                else -> (System.currentTimeMillis() - lastEvent.endTime) / 1000
            }
            delay(1000)
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .clickable { onActionClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = when {
                    lastEvent == null -> stringResource(R.string.no_task)
                    lastEvent.endTime == null -> lastEvent.name ?: stringResource(R.string.no_task)
                    else -> stringResource(R.string.no_task)
                },
                color = BlackPine, fontWeight = Bold, fontFamily = Montserrat,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (lastEvent?.endTime == null && lastEvent != null) {
                lastEvent.projectId?.let { projectId ->
                    projects.find { it.projectId == projectId }?.let { project ->
                        ProjectTag(text = project.title, color = Color(project.color))
                    }
                }
            }
        }
        val timeText = when {
            lastEvent == null -> "     ∞"
            lastEvent.endTime == null -> formatTimeElapsed(timeElapsed)
            else -> formatTimeElapsed(timeElapsed)
        }
        Text(
            timeText,
            color = BlackPine,
            fontWeight = Bold,
            fontFamily = Montserrat,
            fontSize = 20.sp,
            modifier = Modifier
                .width(130.dp)
                .padding(horizontal = 12.dp)
        )
        Row {
            Box(
                modifier = Modifier
                    .clickable { onCircleButtonClick() }
                    .clip(CircleShape)
                    .background(PineColor)
                    .size(45.dp)
            ) {
                Image(
                    painter = painterResource(if (lastEvent?.endTime == null && lastEvent != null) R.drawable.stop else R.drawable.play),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(21.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

fun formatTimeElapsed(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
} 