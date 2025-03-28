import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmitrysukhov.lifetracker.R
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.DarkerPine
import com.dmitrysukhov.lifetracker.utils.Green
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.util.*

@Composable
fun NewTaskScreen(setTopBarState: (TopBarState) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTime by remember { mutableStateOf("Завтра 15:00") }
    var reminders by remember { mutableStateOf(listOf("Сегодня 22:30", "Завтра 11:00")) }
    var repeatDays by remember { mutableStateOf(setOf<String>()) }
    var taskDuration by remember { mutableStateOf("00:45:00") }
    val context = LocalContext.current

//По добовлять всем текстам вес и шрифт
    val globalTextStyle = TextStyle(
        fontFamily = FontFamily(Font(R.font.montserrat_regular)),
        fontSize = 16.sp,
        color = Color.Black
    )

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = "New Task",
                topBarActions = {
                    if (title.text.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Сохранить",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                saveTask(title.text, description.text)
                                Toast.makeText(context, "Задача сохранена", Toast.LENGTH_SHORT)
                                    .show()
                            }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.text.isEmpty()) Text(
                            "Заголовок",
                            fontSize = 18.sp,
                            fontWeight = W700,
                            fontFamily = Montserrat,
                            color = Green
                        )
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(Modifier.width(352.dp))

            BasicTextField(
                value = description,
                onValueChange = { description = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp, fontWeight = W500,
                    fontFamily = Montserrat,
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (description.text.isEmpty()) Text(
                            "Описание",
                            fontSize = 16.sp,
                            fontWeight = W500,
                            fontFamily = Montserrat,
                            color = Green
                        )
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(Modifier.width(352.dp))

            TaskOption("$selectedTime", R.drawable.data) {
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
            Divider(Modifier.width(352.dp))
            TaskOption("Покупки", R.drawable.proekt, showIcon = true) {}
            Divider(Modifier.width(352.dp))
            TaskOption("Напоминания", R.drawable.bell) {}


            //Сделать функцию добовления
            //заменить крестик как на дизайне,тонкий и серый
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .width(120.dp)
            ) {
                reminders.forEach { reminder ->
                    Text(
                        text = "$reminder ❌",
                        fontSize = 14.sp,
                        color = DarkerPine,
                        fontWeight = W500,
                        fontFamily = Montserrat,
                        modifier = Modifier.clickable { reminders = reminders - reminder }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            //На до каждой букве добавить Фон белый с салатовой обвоткой а принажатие полностью становится салатовым
            Divider(Modifier.width(352.dp))
            TaskOption("Повторение", R.drawable.repeat) {}
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 16.sp,
                        fontWeight = W500,
                        fontFamily = Montserrat,
                        color = if (repeatDays.contains(day)) PineColor else Color.Gray,
                        modifier = Modifier
                            .clickable {
                                repeatDays =
                                    if (repeatDays.contains(day)) repeatDays - day else repeatDays + day
                            }
                            .padding(4.dp)
                    )
                }
            }
            Divider(
                Modifier
                    .width(352.dp)
                    .padding(16.dp))
            TaskOption("Время на задачу", R.drawable.vremya) {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        taskDuration = String.format("%02d:%02d:00", hour, minute)
                    },
                    0, 45, true
                ).show()
            }
            //переместить в лево
            Text(
                text = taskDuration, fontSize = 16.sp, fontWeight = W500,
                fontFamily = Montserrat, color = PineColor
            )
            Divider(Modifier.width(352.dp))
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
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = PineColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 16.sp)
        }
        if (showIcon) {
            Icon(
                painter = painterResource(id = R.drawable.ministrelka),
                contentDescription = "Expand",
                tint = PineColor
            )
        }
    }
}

fun saveTask(title: String, description: String) {
    // Реальная логика сохранения в БД или SharedPreferences
}


@Preview(showBackground = true)
@Composable
fun PreviewNewTaskScreen() {
    NewTaskScreen {}
}

const val NEW_TASK_SCREEN = "NewTaskScreen"
