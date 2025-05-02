package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.Event
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale

@Composable
fun TrackerTimeline(
    events: List<Event>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit,
    projects: List<Project>, onEventClick: (Event) -> Unit, modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val hourHeight = 80.dp
    val halfHourHeight = 40.dp
    val dateFormatter = DateTimeFormat.forPattern("d MMMM yyyy")
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous day", tint = PineColor,
                modifier = Modifier
                    .clickable { onDateSelected(selectedDate.minusDays(1)) }
                    .padding(8.dp)
            )

            Text(
                text = dateFormatter.print(selectedDate),
                color = PineColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next day", tint = PineColor,
                modifier = Modifier
                    .clickable { onDateSelected(selectedDate.plusDays(1)) }
                    .padding(8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time column
                Column(modifier = Modifier.width(50.dp)) {
                    for (hour in 0..23) {
                        Column(modifier = Modifier.height(hourHeight)) {
                            Box(
                                modifier = Modifier
                                    .height(halfHourHeight)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:00", hour),
                                    color = PineColor,
                                    fontSize = 14.sp,
                                    fontFamily = Montserrat
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(halfHourHeight)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:30", hour),
                                    color = PineColor,
                                    fontSize = 14.sp,
                                    fontFamily = Montserrat
                                )
                            }
                        }
                    }
                }

                // Events column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(hourHeight * 24)
                ) {
                    // Timeline dividers
                    Column {
                        repeat(48) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(halfHourHeight),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                HorizontalDivider(thickness = 1.dp, color = PineColor.copy(0.5f))
                            }
                        }
                    }

                    // Events
                    events.forEach { event ->
                        val startTime = DateTime(event.startTime)
                        val endTime = event.endTime?.let { DateTime(it) } ?: DateTime.now()
                        val dayStart = DateTime(selectedDate.year, selectedDate.monthOfYear, selectedDate.dayOfMonth, 0, 0)
                        
                        // Проверяем пересечение с текущим днем
                        if (endTime.isBefore(dayStart) || startTime.isAfter(dayStart.plusDays(1))) return@forEach
                        
                        // Корректируем время для событий, начавшихся в предыдущем дне
                        val adjustedStart = if (startTime.isBefore(dayStart)) dayStart else startTime
                        val adjustedEnd = if (endTime.isAfter(dayStart.plusDays(1))) dayStart.plusDays(1) else endTime
                        
                        val startMinutes = ((adjustedStart.millis - dayStart.millis) / 60000).toInt()
                        val durationMinutes = Duration(adjustedStart, adjustedEnd).standardMinutes
                        val topPadding = (startMinutes * 4f / 3f).dp + 19.dp
                        val height = (durationMinutes * 4f / 3f).dp
                        val project = projects.find { it.projectId == event.projectId }
                        val color = project?.let { Color(it.color) } ?: Color(0xFF4CAF50)

                        EventBlock(
                            event = event,
                            color = color,
                            projects = projects,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height)
                                .offset(y = topPadding)
                                .clickable { onEventClick(event) }
                        )
                    }

                    // Current time indicator
                    val now = DateTime.now()
                    if (now.toLocalDate() == selectedDate) {
                        val currentMinutes = now.hourOfDay * 60 + now.minuteOfHour
                        val currentPosition = (currentMinutes * 4f / 3f).dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = currentPosition + 20.dp)
                                .background(Color.Red)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventBlock(
    event: Event, color: Color = PineColor, projects: List<Project>, modifier: Modifier = Modifier
) {
    val startTime = DateTime(event.startTime)
    val endTime = event.endTime?.let { DateTime(it) } ?: DateTime.now()
    val duration = Duration(startTime, endTime)
    val durationMinutes = duration.standardMinutes
    val isShortEvent = durationMinutes < 30
    val project = projects.find { it.projectId == event.projectId }
    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (isShortEvent) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name ?: stringResource(R.string.no_name), color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat,
                    maxLines = 1
                )
                Text(
                    text = formatDuration(durationMinutes), color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp, fontFamily = Montserrat, maxLines = 1
                )
            }
        } else Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = event.name ?: stringResource(R.string.no_name), color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat,
                maxLines = 1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project?.title ?: stringResource(R.string.no_project), maxLines = 1,
                    color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                    fontFamily = Montserrat,
                )
                Text(
                    text = formatDuration(durationMinutes), color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp, fontFamily = Montserrat, maxLines = 1
                )
            }
        }
    }
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) String.format(Locale.getDefault(), "%dh %dm", hours, remainingMinutes)
    else String.format(Locale.getDefault(), "%dm", remainingMinutes)
}