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
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = "New Task",
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
                            tint = Color.White
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
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(fontSize = 20.sp, color = Color.Gray),
            decorationBox = { innerTextField ->
                if (title.text.isEmpty()) {
                    Text("Заголовок", fontSize = 20.sp, color = PineColor, fontWeight = W700)
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        Divider(color = Color.LightGray, thickness = 1.dp)
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Gray),
            decorationBox = { innerTextField ->
                if (description.text.isEmpty()) {
                    Text("Описание", fontSize = 16.sp, color = PineColor, fontWeight = W500)
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Divider(color = Color.LightGray, thickness = 1.dp)

        TaskOption("Дата/время", R.drawable.data)
        Divider(color = Color.LightGray, thickness = 1.dp)

        TaskOption("Проект", R.drawable.proekt, showIcon = true)
        Divider(color = Color.LightGray, thickness = 1.dp)

        TaskOption("Добавить напоминание", R.drawable.kolokol)
        Divider(color = Color.LightGray, thickness = 1.dp)

        TaskOption("Добавить повторение", R.drawable.strelki)
        Divider(color = Color.LightGray, thickness = 1.dp)

        TaskOption("Добавить время на задачу", R.drawable.vremya)
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}

@Composable
fun TaskOption(text: String, iconRes: Int, showIcon: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
            Text(text, fontSize = 16.sp, color = Color.Black)
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

