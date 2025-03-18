package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmitrysukhov.lifetracker.AppDatabase
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.MyApplicationTheme
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.TodoItem
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun TodoListScreen(setTopBarState: (TopBarState) -> Unit, ) {
    MyApplicationTheme {
        val context = LocalContext.current
        val todoDao = AppDatabase.getDatabase(context).todoDao()
        val viewModel: TodoViewModel = viewModel(factory = TodoViewModelFactory(todoDao))
        val todoList by viewModel.todoList.collectAsState()
        var taskText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .background(BgColor)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val style = TextStyle(fontWeight = Bold, fontFamily = Montserrat)
                TextField(
                    textStyle = style,
                    value = taskText,
                    onValueChange = { taskText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите задачу", style = style) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (taskText.isNotBlank()) {
                                viewModel.addTask(taskText)
                                taskText = ""
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (taskText.isNotBlank()) {
                            viewModel.addTask(taskText)
                            taskText = ""
                        }
                    }
                ) { Text("Добавить", fontFamily = Montserrat) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                Modifier
                    .padding(horizontal = 24.dp)

            ) {
                items(todoList) { todoItem ->
                    TodoListItem(
                        item = todoItem,
                        onCheckedChange = { isChecked -> viewModel.updateTask(todoItem.copy(isDone = isChecked)) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TodoListItem(item: TodoItem, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

        )
    {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(if (item.isDone) R.drawable.checked else R.drawable.not_checked),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onCheckedChange(!item.isDone) }
                    .size(20.dp)
            )
            Spacer(
                Modifier
                    .size(2.dp, 34.dp)
                    .padding(top = 4.dp)
                    .background(color = PineColor)
            )

        }
        Column {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.text, fontFamily = Montserrat,
                    fontSize = 14.sp, fontWeight = Medium,
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
                ProjectTag(text = "Покупки")
            }
            Row {

            }


        }
    }
}

val TODOLIST_SCREEN = "Todo List"

@Composable
fun ProjectTag(text: String) {
    Box(Modifier
        .background(Color.Gray, shape = RoundedCornerShape(52))
        .padding(horizontal = 8.dp, vertical = 1.dp)) {
        Text(
            fontFamily = Montserrat,
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = Medium,
        )
    }


}