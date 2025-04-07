package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.*

@Composable
fun <TodoViewModel> TodoListScreen(
    setTopBarState: (TopBarState) -> Unit,
    viewModel: TodoViewModel,
    navController: NavHostController
) {
    val todoList by viewModel.todoList.collectAsState()

    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(
                title = "Todo List",
                topBarActions = {}
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(todoList) { todoItem ->
            TodoItemRow(todoItem) {

                viewModel.setSelectedTask(todoItem)
                navController.navigate("NewTaskScreen")
            }
        }
    }
}



@Composable
fun TodoItemRow(
    todoItem: Int,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() }
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.task),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Black
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = todoItem.text,
                style = TextStyle(fontSize = 18.sp),
                color = Color.Black
            )
            todoItem.description?.let {
                Text(
                    text = it,
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                    maxLines = 1
                )
            }
        }
    }
}

const val TODOLIST_SCREEN = "todoListScreen"