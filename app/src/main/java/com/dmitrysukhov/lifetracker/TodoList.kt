package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

//это экран Список дел, но он не подключен к базе данных
@Composable
fun TodoListScreen() {
    var taskText by remember { mutableStateOf("") }
    var todoList by remember { mutableStateOf(listOf<TodoItem>()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = taskText,
                onValueChange = { taskText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Введите задачу") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (taskText.isNotBlank()) {
                            todoList = todoList + TodoItem(text = taskText, isDone = false)
                            taskText = ""
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (taskText.isNotBlank()) {
                        todoList = todoList + TodoItem(text = taskText, isDone = false)
                        taskText = ""
                    }
                }
            ) {
                Text("Добавить")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Список задач
        TodoList(todoList = todoList, onTaskCheckedChange = { index, isChecked ->
            val updatedList = todoList.toMutableList()
            updatedList[index] = updatedList[index].copy(isDone = isChecked)
            todoList = updatedList
        }, onDeleteTask = { index ->
            todoList = todoList.toMutableList().apply { removeAt(index) }
        })
    }
}

@Composable
fun TodoList(
    todoList: List<TodoItem>,
    onTaskCheckedChange: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    Column {
        todoList.forEachIndexed { index, todoItem ->
            TodoListItem(
                item = todoItem,
                onCheckedChange = { isChecked -> onTaskCheckedChange(index, isChecked) },
                onDelete = { onDeleteTask(index) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TodoListItem(item: TodoItem, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isDone,
                onCheckedChange = onCheckedChange
            )
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Удалить задачу"
            )
        }
    }
}
//кчау

const val TODOLIST_SCREEN = "TodoList"