package com.dmitrysukhov.lifetracker.todo

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.*
import java.util.*

@Composable
fun NewTaskScreen(
    setTopBarState: (TopBarState) -> Unit, viewModel: TodoViewModel,
    navController: NavHostController
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable { mutableStateOf("Завтра 15:00") }
    var reminders by rememberSaveable { mutableStateOf(listOf("Сегодня 22:30", "Завтра 11:00")) }
    var repeatDays by rememberSaveable { mutableStateOf(setOf<String>()) }
    var taskDuration by rememberSaveable { mutableStateOf("00:45:00") }
    val context = LocalContext.current

    val globalTextStyle = TextStyle(
        fontFamily = FontFamily(Font(R.font.montserrat_regular)), fontSize = 16.sp,
        color = Color.Black
    )

    val topBarTitle = stringResource(R.string.new_task)
    val saveToastText = stringResource(R.string.save_task_toast)

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = topBarTitle,
                topBarActions = {
                    if (title.isNotEmpty()) IconButton({
                        viewModel.addTask(title)
                        Toast.makeText(context, saveToastText, Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.tick),
                            contentDescription = null, tint = Color.White,
                        )
                    }
                }
            )
        )
    }

    CompositionLocalProvider(LocalTextStyle provides globalTextStyle) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            BasicTextField(
                value = title, onValueChange = { title = it },
                textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) Text(
                            stringResource(R.string.title_hint),
                            fontSize = 18.sp,
                            fontWeight = W700,
                            fontFamily = Montserrat,
                            color = PineColor.copy(0.5f)
                        )
                        innerTextField()
                    }
                }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            BasicTextField(
                value = description, onValueChange = { description = it },
                textStyle = SimpleText.copy(color = InverseColor),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (description.isEmpty()) Text(
                            stringResource(R.string.description_hint),
                            fontSize = 16.sp,
                            fontWeight = W500,
                            fontFamily = Montserrat,
                            color = PineColor.copy(0.5f)
                        )
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            TaskOption(selectedTime, R.drawable.data) {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        selectedTime = "$hour:$minute"
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
            HorizontalDivider()
            TaskOption(stringResource(R.string.shopping), R.drawable.proekt, showIcon = true) {}
            HorizontalDivider()
            TaskOption(stringResource(R.string.reminders), R.drawable.bell) {}

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                reminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .background(AccentColor, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = reminder, style = SimpleText, color = DarkerPine)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.krest),
                            contentDescription = null, tint = DarkerPine,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { reminders = reminders - reminder }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            HorizontalDivider()
            TaskOption(stringResource(R.string.repeat), R.drawable.repeat) {}
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                days.forEach { day ->
                    val isSelected = repeatDays.contains(day)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AccentColor else Color.Unspecified)
                            .border(2.dp, AccentColor, CircleShape)
                            .clickable {
                                repeatDays = if (isSelected) repeatDays - day else repeatDays + day
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day, style = SimpleText,
                            color = if (isSelected) Color.Black else InverseColor
                        )
                    }
                }
            }
            HorizontalDivider()

            TaskOption(stringResource(R.string.time_for_task), R.drawable.vremya) {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        taskDuration =
                            String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute)
                    }, 0, 45, true
                ).show()
            }

            Row(
                modifier = Modifier
                    .background(AccentColor, shape = RoundedCornerShape(15.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = taskDuration, style = H2, color = InverseColor)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun TaskOption(text: String, iconRes: Int, showIcon: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = PineColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = H2, color = InverseColor)
        }
        if (showIcon) Icon(
            painter = painterResource(id = R.drawable.ministrelka),
            contentDescription = null, tint = PineColor
        )
    }
}

const val NEW_TASK_SCREEN = "NewTaskScreen"
