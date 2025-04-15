package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TimelineColor
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

@Composable
fun TrackerTimeline(
    events: List<Event>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val hourHeight = 80.dp
    val dateFormatter = DateTimeFormat.forPattern("d MMMM yyyy")
    
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Date navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous day",
                tint = PineColor,
                modifier = Modifier
                    .clickable { onDateSelected(selectedDate.minusDays(1)) }
                    .padding(8.dp)
            )
            
            Text(
                text = dateFormatter.print(selectedDate),
                color = PineColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next day",
                tint = PineColor,
                modifier = Modifier
                    .clickable { onDateSelected(selectedDate.plusDays(1)) }
                    .padding(8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Time scale
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .height((24 * 80).dp)
            ) {
                for (hour in 0..23) {
                    Column(
                        modifier = Modifier.height(hourHeight)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = String.format("%02d:00", hour),
                                color = PineColor,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = String.format("%02d:30", hour),
                                color = PineColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Box {
                Column {
                    repeat(48) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            HorizontalDivider(thickness = 1.dp, color = TimelineColor)
                        }
                    }
                }

                events.forEach { event ->
                    val startTime = DateTime(event.startTime)
                    val endTime = event.endTime?.let { DateTime(it) } ?: DateTime.now()
                    val adjustedStartTime = if (startTime.toLocalDate() == endTime.toLocalDate())
                        startTime else startTime.withTime(LocalTime(0, 0))
                    val startMinutes = adjustedStartTime.hourOfDay * 60 + adjustedStartTime.minuteOfHour
                    val duration = Duration(adjustedStartTime, endTime)
                    val durationMinutes = duration.standardMinutes
                    
                    val topPadding = ((startMinutes * 80) / 60).dp
                    val height = ((durationMinutes * 80) / 60).toInt().dp

                    EventBlock(
                        event = event,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .padding(horizontal = 8.dp)
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.place(0, topPadding.roundToPx())
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun EventBlock(
    event: Event,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF4CAF50),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = event.name ?: "Без названия",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                text = "Project ${event.projectId}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
} 