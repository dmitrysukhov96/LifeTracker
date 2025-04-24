package com.dmitrysukhov.lifetracker.habits


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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Habit
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale

@Composable
fun HabitScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle(emptyList())
    var mode by rememberSaveable { mutableStateOf(HabitViewMode.WEEK) }
    var currentDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val periodTitle = when (mode) {
        HabitViewMode.WEEK -> {
            val start = currentDate.withDayOfWeek(1)
            val end = start.withDayOfWeek(7)
            "${start.toString("dd.MM.yyyy")} - ${end.toString("dd.MM.yyyy")}"
        }
        HabitViewMode.MONTH -> currentDate.toString("MMMM yyyy")
        HabitViewMode.YEAR -> currentDate.toString("yyyy")
    }

    var dialogData by remember { mutableStateOf<Pair<Habit, LocalDate>?>(null)}
    var numberInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState("Habits") {
                IconButton(onClick = { navController.navigate(NEW_HABIT_SCREEN) }) {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
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
        Spacer(modifier = Modifier.height(16.dp))
        ThreeButtonsSelector(mode.ordinal, stringResource(R.string.week),
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
                val eventsMap by viewModel.getEventsForHabit(habit.id).collectAsState(initial = emptyMap())

                HabitCard(
                    habit = habit,
                    mode = mode,
                    currentDate = currentDate,
                    onSquareClick = { date ->
                        if (HabitType.entries[habit.type] == HabitType.CHECKBOX) {
                            viewModel.saveHabitEvent(habit.id, date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis, 1)
                        } else { dialogData = habit to date }
                    },
                    events = eventsMap
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (dialogData != null) {
        AlertDialog(
            onDismissRequest = { dialogData = null },
            confirmButton = {
                TextButton(onClick = {
                    val (habit, date) = dialogData!!
                    viewModel.saveHabitEvent(habit.id, date.toDateTimeAtStartOfDay(DateTimeZone.UTC).millis, numberInput.toIntOrNull() ?: 0)
                    dialogData = null
                    numberInput = ""
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogData = null
                    numberInput = ""
                }) {
                    Text("Отмена")
                }
            },
            title = { Text("Введите значение") },
            text = {
                OutlinedTextField(
                    value = numberInput,
                    onValueChange = { numberInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    mode: HabitViewMode,
    currentDate: LocalDate,
    onSquareClick: (LocalDate) -> Unit,
    events: Map<Long, Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(habit.color))
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Text(habit.title, style = H2.copy(color = Color.White, fontWeight = Bold))
        Spacer(modifier = Modifier.height(8.dp))

        val isMarked: (LocalDate) -> Boolean = {
            events.containsKey(it.toDateTimeAtStartOfDay().millis)
        }

        when (mode) {
            HabitViewMode.MONTH -> {
                val days = generateMonthDays(currentDate)
                val weeks = days.chunked(7)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    weeks.forEach { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            week.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                day == null -> Color.Transparent
                                                isMarked(day) -> Color.White
                                                else -> Color.White.copy(0.5f)
                                            }
                                        )
                                        .clickable(enabled = day != null) {
                                            day?.let(onSquareClick)
                                        }
                                )
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
                                    modifier = Modifier.width(50.dp)
                                )
                                monthDays.forEachIndexed { dayIndex, isDayInMonth ->
                                    val date = try {
                                        LocalDate(year, monthIndex + 1, dayIndex + 1)
                                    } catch (_: IllegalArgumentException) {
                                        null
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    !isDayInMonth || date == null -> Color.Transparent
                                                    isMarked(date) -> Color.White
                                                    else -> Color.White.copy(alpha = 0.5f)
                                                }
                                            )
                                            .clickable(enabled = isDayInMonth && date != null) {
                                                date?.let(onSquareClick)
                                            }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            }

            HabitViewMode.WEEK -> {
                val start = currentDate.withDayOfWeek(1)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { i ->
                        val date = start.plusDays(i.toInt())
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isMarked(date)) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                                .clickable { onSquareClick(date) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ThreeButtonsSelector(selected: Int, first: String, second: String, third: String, onSelect:(Int)-> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Button(colors = buttonColors(containerColor = PineColor.copy(if (selected == 0) 1f else 0.4f)), onClick = {onSelect(0)}) { Text(first, style = H2) }
        Button(colors = buttonColors(containerColor = PineColor.copy(if (selected == 1) 1f else 0.4f)), onClick = {onSelect(1)}) { Text(second, style = H2) }
        Button(colors = buttonColors(containerColor = PineColor.copy(if (selected == 2) 1f else 0.4f)), onClick = {onSelect(2)}) { Text(third, style = H2) }
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

