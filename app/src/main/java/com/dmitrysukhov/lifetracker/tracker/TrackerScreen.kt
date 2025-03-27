package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.todo.formatTime
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import kotlinx.coroutines.delay

@Composable
fun TrackerScreen(
    setTopBarState: (TopBarState) -> Unit,
//    navController: NavHostController,
    trackerViewModel: TrackerViewModel = hiltViewModel()
) {
    // Подписываемся на последнее событие
    val lastEvent by trackerViewModel.lastEvent.collectAsState()
    val todayEvents by trackerViewModel.todayEvents.collectAsState()

    val currentTime = System.currentTimeMillis()

    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("Tracker"))
    }

    // Время с последнего обновления
    var timeElapsed by remember { mutableLongStateOf(0L) }

    // Тикать каждую секунду
    LaunchedEffect(lastEvent) {
        while (true) {
            if (lastEvent != null && lastEvent?.endTime == null) {
                timeElapsed = (System.currentTimeMillis() - (lastEvent?.startTime ?: 0)) / 1000
            }
            delay(1000)  // Задержка в 1 секунду для обновления времени
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        if (lastEvent != null) {
            val event = lastEvent
            if (event?.endTime == null) {
                // Ивент еще не завершён
                Text("Время с начала: $timeElapsed секунд")
                Button(onClick = { trackerViewModel.stopEvent() }) {
                    Text("Stop")
                }
            } else {
                // Ивент завершён
                val timeSinceEnd = (currentTime - event.endTime) / 1000
                Text("Без задачи: $timeSinceEnd секунд")
                Button(onClick = { trackerViewModel.startEvent(1) }) {
                    Text("Play")
                }
            }
        } else {
            Text("Нет активных ивентов")
            Button(onClick = { trackerViewModel.startEvent(1) }) {
                Text("Start")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Сегодняшние события:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
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
                Text("• $start – $end", fontSize = 14.sp)
            }
        }
    }
}

const val TRACKER_SCREEN = "Tracker"