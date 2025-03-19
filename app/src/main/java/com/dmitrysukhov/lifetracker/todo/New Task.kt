package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.tooling.preview.Preview
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState


@Composable
fun NewTaskScreen(setTopBarState: (TopBarState) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue("LifeTracker")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = "LifeTracker",
                leftIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.strelka),
                            contentDescription = "Далее",
                            tint = Color.White
                        )
                    }
                },
                rightIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Удалить",
                            tint = Color.White )
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

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TaskOption("Дата/время", R.drawable.data)
        TaskOption("Проект", R.drawable.proekt, showIcon = true)
        TaskOption("Добавить напоминание", R.drawable.kolokol)
        TaskOption("Добавить повторение", R.drawable.strelki)
        TaskOption("Добавить время на задачу", R.drawable.vremya)
    }
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