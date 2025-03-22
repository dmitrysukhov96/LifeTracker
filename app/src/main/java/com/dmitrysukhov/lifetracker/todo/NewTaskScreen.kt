package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NewTaskScreen(setTopBarState: (TopBarState) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = "LifeTracker",
                topBarActions = {
                    if (title.text.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Сохранить",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                saveTask(title.text, description.text)
                            }
                        )
                    }
                }
            )
        )
    }

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
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp,
                color = PineColor,
                textAlign = TextAlign.Start
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.text.isEmpty()) Text("Заголовок", fontSize = 18.sp, color = Color.Gray)
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = PineColor,
                textAlign = TextAlign.Start
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (description.text.isEmpty()) Text("Описание", fontSize = 16.sp, color = Color.Gray)
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TaskOption("Дата/время", R.drawable.data)
        TaskOption("Проект", R.drawable.proekt, showIcon = true)
        TaskOption("Добавить напоминание", R.drawable.bell)
        TaskOption("Добавить повторение", R.drawable.repeat)
        TaskOption("Добавить время на задачу", R.drawable.vremya)
    }
}

fun saveTask(title: String, description: String) {
    // тут вроде как должна быть логига
}

@Composable
fun TaskOption(text: String, iconRes: Int, showIcon: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

@Preview(showBackground = true)
@Composable
fun PreviewNewTaskScreen() {
    NewTaskScreen {}
}

const val NEW_TASK_SCREEN = "NewTaskScreen"
