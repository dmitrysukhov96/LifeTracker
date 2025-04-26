package com.dmitrysukhov.lifetracker.habits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Habit
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.EmptyPlaceholder
import com.dmitrysukhov.lifetracker.common.ui.ThreeButtonsSelector
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.TopBarState
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale

@Composable
fun HabitScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: HabitsViewModel
) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsStateWithLifecycle(emptyList())
    var mode by rememberSaveable { mutableStateOf(HabitViewMode.WEEK) }
    var currentDate by rememberSaveable { mutableStateOf(LocalDate.now(DateTimeZone.UTC)) }

    val periodTitle = when (mode) {
        HabitViewMode.WEEK -> {
            val start = currentDate.withDayOfWeek(1)
            val end = start.withDayOfWeek(7)
            "${start.toString("dd.MM.yyyy")} - ${end.toString("dd.MM.yyyy")}"
        }

        HabitViewMode.MONTH -> currentDate.toString("MMM yyyy")
        HabitViewMode.YEAR -> currentDate.toString("yyyy")
    }

    var dialogData by remember { mutableStateOf<Pair<Habit, LocalDate>?>(null) }
    var numberInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(context.getString(R.string.habits)) {
                IconButton(onClick = {
                    viewModel.selectedHabit = null
                    navController.navigate(NEW_HABIT_SCREEN)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.plus), contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 16.dp)
    ) {
        if (habits.isEmpty()) EmptyPlaceholder(R.string.no_habits, R.string.create_habit_hint)
        else {
            Spacer(modifier = Modifier.height(16.dp))
            ThreeButtonsSelector(
                mode.ordinal, stringResource(R.string.week),
                stringResource(R.string.month), stringResource(R.string.year)
            ) { mode = HabitViewMode.entries[it] }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentDate = when (mode) {
                        HabitViewMode.WEEK -> currentDate.minusWeeks(1)
                        HabitViewMode.MONTH -> currentDate.minusMonths(1)
                        HabitViewMode.YEAR -> currentDate.minusYears(1)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                Text(periodTitle, style = H2)
                IconButton(onClick = {
                    currentDate = when (mode) {
                        HabitViewMode.WEEK -> currentDate.plusWeeks(1)
                        HabitViewMode.MONTH -> currentDate.plusMonths(1)
                        HabitViewMode.YEAR -> currentDate.plusYears(1)
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(habits.size) { index ->
                    val habit = habits[index]
                    val eventsMap by viewModel.getEventsForHabit(habit.id)
                        .collectAsStateWithLifecycle(emptyMap())

                    HabitCard(
                        habit = habit,
                        mode = mode,
                        currentDate = currentDate,
                        onSquareClick = { date ->
                            if (habit.type < HabitType.entries.size && HabitType.entries[habit.type] == HabitType.CHECKBOX) {
                                val dateMillis =
                                    date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis
                                if (eventsMap.containsKey(dateMillis)) {
                                    viewModel.deleteHabitEvent(habit.id, dateMillis)
                                } else viewModel.saveHabitEvent(habit.id, dateMillis, 1f)
                            } else dialogData = habit to date
                        },
                        events = eventsMap
                    ) {
                        viewModel.selectedHabit = habit
                        navController.navigate(NEW_HABIT_SCREEN)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(48.dp)) }
            }
        }
    }

    if (dialogData != null) {
        val (habit, date) = dialogData!!
        val dateMillis = date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis
        val existingValue = viewModel.getEventsForHabit(habit.id)
            .collectAsStateWithLifecycle(emptyMap()).value[dateMillis]

        LaunchedEffect(existingValue) {
            if (existingValue != null) {
                numberInput = if (existingValue.toInt().toFloat() == existingValue)
                    existingValue.toInt().toString() else existingValue.toString()
            }
        }

        AlertDialog(
            containerColor = BgColor,
            onDismissRequest = {
                dialogData = null
                numberInput = ""
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (numberInput.isNotBlank()) {
                            viewModel.saveHabitEvent(
                                habit.id,
                                dateMillis,
                                numberInput.toFloatOrNull() ?: 0f
                            )
                            dialogData = null
                            numberInput = ""
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = SimpleText.copy(fontFamily = Montserrat)
                    )
                }
            },
            dismissButton = {
                Row {
                    if (existingValue != null) {
                        TextButton(
                            onClick = {
                                viewModel.deleteHabitEvent(habit.id, dateMillis)
                                dialogData = null
                                numberInput = ""
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = stringResource(R.string.delete),
                                tint = PineColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            dialogData = null
                            numberInput = ""
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = SimpleText.copy(fontFamily = Montserrat)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.enter_value),
                    style = H2.copy(fontFamily = Montserrat)
                )
            },
            text = {
                OutlinedTextField(
                    value = numberInput,
                    onValueChange = { numberInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = SimpleText.copy(fontFamily = Montserrat)
                )
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit, mode: HabitViewMode, currentDate: LocalDate, onSquareClick: (LocalDate) -> Unit,
    events: Map<Long, Float>, onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(habit.color))
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart
        ) {
            Text(
                habit.title,
                style = H2.copy(color = Color.White, fontWeight = Bold),
                modifier = Modifier.padding(end = 44.dp)
            )
            IconButton(
                onClick = { onEdit() }, modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            { Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        when (mode) {
            HabitViewMode.WEEK -> {
                val start = currentDate.withDayOfWeek(1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    repeat(7) { i ->
                        val date = start.plusDays(i.toInt())
                        var habitValue =
                            events[date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis]
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(indication = null, interactionSource = null)
                                { onSquareClick(date) }
                                .background(Color.White.copy(alpha = if (habitValue != null) 0.5f else 0.3f))
                        ) {
                            val isCurrentDay = date.isEqual(LocalDate.now())
                            Text(
                                date.toString("d"),
                                style = Small,
                                color = if (isCurrentDay) Color.White else Color.White.copy(0.7f),
                                fontWeight = if (isCurrentDay) Bold else null,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                            val textStyle = if (habitValue.toString().length > 3) SimpleText else H1
                            if (habit.type == 0 && habitValue == 1f) Image(
                                painterResource(R.drawable.tick), null, modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(start = 1.dp, top = 4.dp)
                                    .size(20.dp)
                            ) else if (habitValue != null) Text(
                                if (habitValue.toInt().toFloat() == habitValue)
                                    habitValue.toInt().toString() else habitValue.toString(),
                                style = textStyle.copy(color = Color.White, fontWeight = Bold),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            HabitViewMode.MONTH -> {
                val days = generateMonthDays(currentDate)
                val weeks = days.chunked(7)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    weeks.forEach { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            week.forEach { day ->
                                var habitValue =
                                    events[day?.toDateTimeAtStartOfDay(DateTimeZone.UTC)?.millis]
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                day == null -> Color.Transparent
                                                habitValue != null -> Color.White.copy(0.5f)
                                                else -> Color.White.copy(0.3f)
                                            }
                                        )
                                        .clickable(enabled = day != null) { day?.let(onSquareClick) }
                                ) {
                                    if (habitValue == null) {
                                        val isCurrentDay = day?.isEqual(LocalDate.now()) == true
                                        Text(
                                            day?.toString("d") ?: "",
                                            style = Small, color = if (isCurrentDay) Color.White
                                            else Color.White.copy(0.7f),
                                            fontWeight = if (isCurrentDay) Bold else null,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else if (habit.type == 0 && habitValue == 1f) Image(
                                        painterResource(R.drawable.tick), null,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(16.dp)
                                    ) else Text( //todo animate
                                        if (habitValue.toInt().toFloat() == habitValue)
                                            habitValue.toInt().toString() else habitValue.toString(),
                                        style = Small.copy(
                                            color = Color.White, fontWeight = Bold,
                                            lineHeight = 12.sp
                                        ),
                                        modifier = Modifier.align(Alignment.Center),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HabitViewMode.YEAR -> {
                val year = currentDate.year().get()
                val months = (1..12).map { month ->
                    val daysInMonth = LocalDate(year, month, 1).dayOfMonth().maximumValue
                    List(31) { day -> day < daysInMonth }
                }
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        months.forEachIndexed { monthIndex, monthDays ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                val monthName = DateTimeFormat.forPattern("MMM")
                                    .withLocale(Locale.getDefault())
                                    .print(LocalDate(year, monthIndex + 1, 1))
                                Text(
                                    text = monthName,
                                    color = Color.White, style = SimpleText,
                                    modifier = Modifier.width(48.dp), maxLines = 1
                                )
                                monthDays.forEachIndexed { dayIndex, isDayInMonth ->
                                    val date = try {
                                        LocalDate(year, monthIndex + 1, dayIndex + 1)
                                    } catch (_: IllegalArgumentException) {
                                        null
                                    }
                                    var habitValue =
                                        events[date?.toDateTimeAtStartOfDay(DateTimeZone.UTC)?.millis]
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    !isDayInMonth || date == null -> Color.Transparent
                                                    habitValue != null -> Color.White.copy(0.4f)
                                                    else -> Color.White.copy(alpha = 0.2f)
                                                }
                                            )
                                            .clickable(enabled = isDayInMonth && date != null) {
                                                date?.let { onSquareClick(it) }
                                            }
                                    ) {
                                        if (habitValue == null) {
                                            val isCurrentDay =
                                                date?.isEqual(LocalDate.now()) == true
                                            Text(
                                                date?.toString("d") ?: "", style = Small,
                                                color = if (isCurrentDay) Color.White
                                                else Color.White.copy(0.7f),
                                                fontWeight = if (isCurrentDay) Bold else null,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        } else if (habit.type == 0 && habitValue == 1f) Image(
                                            painterResource(R.drawable.tick), null,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(16.dp)
                                        ) else Text(
                                            if (habitValue.toInt().toFloat() == habitValue)
                                                habitValue.toInt()
                                                    .toString() else habitValue.toString(),
                                            style = Small.copy(
                                                color = Color.White,
                                                fontWeight = Bold,
                                                lineHeight = 12.sp
                                            ),
                                            modifier = Modifier.align(Alignment.Center),
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            }
        }
    }
}

fun generateMonthDays(currentDate: LocalDate): List<LocalDate?> {
    val firstDayOfMonth = currentDate.withDayOfMonth(1)
    val totalDays = currentDate.dayOfMonth().maximumValue
    val startOffset = firstDayOfMonth.dayOfWeek % 7

    return buildList {
        repeat(startOffset) { add(null) }
        for (day in 1..totalDays) {
            add(firstDayOfMonth.withDayOfMonth(day))
        }
    }
}

enum class HabitViewMode { WEEK, MONTH, YEAR }

const val HABIT_SCREEN = "Habits"

enum class HabitType { CHECKBOX, NUMBER }
//todo more better, less better - think how to show it. arrow?

