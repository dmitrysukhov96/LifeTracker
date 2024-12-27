package com.dmitrysukhov.lifetracker

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun TodoListScreen(navController: NavHostController) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    textStyle = InvolveBold,
                    value = taskText,
                    onValueChange = { taskText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите задачу", style = InvolveBold) },
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
                ) {
                    Text("Добавить")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TodoList(
                todoList = todoList,
                onTaskCheckedChange = { item, isChecked ->
                    viewModel.updateTask(item.copy(isDone = isChecked))
                },
                onDeleteTask = { item ->
                    viewModel.deleteTask(item)
                }
            )
        }
    }
}

@Composable
fun TodoList(
    todoList: List<TodoItem>,
    onTaskCheckedChange: (TodoItem, Boolean) -> Unit,
    onDeleteTask: (TodoItem) -> Unit
) {
    Column {
        todoList.forEach { todoItem ->
            TodoListItem(
                item = todoItem,
                onCheckedChange = { isChecked -> onTaskCheckedChange(todoItem, isChecked) },
                onDelete = { onDeleteTask(todoItem) }
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

val TODOLIST_SCREEN = "Todo List"