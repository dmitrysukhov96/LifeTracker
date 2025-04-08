package com.dmitrysukhov.lifetracker.tracker

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.todo.ProjectTag
import com.dmitrysukhov.lifetracker.todo.formatTime
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.Orange
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay

@Composable
fun TrackerScreen(
    setTopBarState: (TopBarState) -> Unit,
    trackerViewModel: TrackerViewModel = hiltViewModel()
) {
    val todayEvents by trackerViewModel.todayEvents.collectAsState()

    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("Tracker"))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        TimeTracker(trackerViewModel)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Сегодняшние события:",
            fontSize = 16.sp,
            fontWeight = Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(todayEvents) { event ->
                val start = formatTime(event.startTime)
                val end = event.endTime?.let { formatTime(it) } ?: "…"
                Text("• $start – $end", style = SimpleText)
            }
        }
    }
}

@Composable
fun TimeTracker(trackerViewModel: TrackerViewModel) {
    val lastEvent by trackerViewModel.lastEvent.collectAsState()
    val currentTime = System.currentTimeMillis()
    var timeElapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastEvent) {
        while (true) {
            if (lastEvent?.endTime == null) {
                timeElapsed = (System.currentTimeMillis() - (lastEvent?.startTime ?: 0)) / 1000
            }
            delay(1000)
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(AccentColor)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Task1lkmefklmfrkmlr lkm23", color = BlackPine, fontWeight = Bold,
                fontFamily = Montserrat, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            ProjectTag("Tag123", Orange)
        }

        val timeText = when {
            lastEvent == null -> ""  // Нет события
            lastEvent?.endTime == null -> formatTimeElapsed(timeElapsed)  // Событие идет
            else -> formatTimeElapsed((currentTime - lastEvent?.endTime!!) / 1000)  // Время без задачи
        }

        Text(
            timeText,
            color = BlackPine,
            fontWeight = Bold,
            fontFamily = Montserrat,
            fontSize = 20.sp,
            modifier = Modifier.width(130.dp).padding(horizontal = 12.dp)
        )

        Row {
            Box(
                modifier = Modifier
                    .clickable {
                        if (lastEvent == null || lastEvent?.endTime != null) {
                            trackerViewModel.startEvent(1)  // Заменить на нужный ID
                        } else {
                            trackerViewModel.stopEvent()
                        }
                    }
                    .clip(CircleShape)
                    .background(PineColor)
                    .size(45.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (lastEvent?.endTime == null) R.drawable.stop else R.drawable.play
                    ),
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
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}
const val TRACKER_SCREEN = "Tracker"