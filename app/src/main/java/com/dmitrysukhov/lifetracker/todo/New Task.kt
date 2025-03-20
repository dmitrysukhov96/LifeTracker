package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
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
                    //todo мусорку пока я спрятал, добавь лучше галочку если !title.isNullOrEmpty()
                    //todo при нажатии на галочку - сохраняется в базу задача, и выходим из этого экрана в предыдущий
                    //как сохранить задачу можешь посмотреть на Туду скрине где "Введите задачу" и кнопка добавить
                    //как выйти назад ты можешь увидеть в кофеШопе при добавлении в корзину

//                    IconButton(onClick = { }) {
//                        Icon(
//                            modifier = Modifier,
//                            painter = painterResource(R.drawable.delete),
//                            contentDescription = "Удалить",
//                            tint = Color.White )
//                    }
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

        OutlinedTextField( //todo тут надо сделать через BasicTextField и в decorationBox написать типа
            //todo it()
            // if (title == null || title == "") Text( "Заголовок" )
            // todo это можно увидеть в МейнАктивити, поищи decorationBox
            //todo также добавить шрифт Монсератт и его цвет, размер, толщину
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField( //todo тут то же самое
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        TaskOption("Дата/время", R.drawable.data) //todo при клике должен появляться пикер. сделаем потом
        TaskOption("Проект", R.drawable.proekt, showIcon = true) //todo тоже пока что рано
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