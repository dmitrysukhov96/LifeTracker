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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.TodoItem
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun TodoListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: TodoViewModel
) {
    val todoList by viewModel.todoList.collectAsState()
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("LifeTracker") {
            IconButton({ navController.navigate(NEW_TASK_SCREEN) }) {
                Icon(painterResource(R.drawable.plus), contentDescription = null, tint = Color.White)
            }
        })
    }
    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(Modifier.padding(horizontal = 24.dp)) {
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
//            Spacer(
//                Modifier
//                    .size(2.dp, 34.dp)
//                    .padding(top = 4.dp)
//                    .background(color = PineColor)
//            )

        }
        Column {
            Row(
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

const val TODOLIST_SCREEN = "Todo List"

@Composable
fun ProjectTag(text: String) {
    Box(
        Modifier
            .background(Color.Gray, shape = RoundedCornerShape(52))
            .padding(horizontal = 8.dp, vertical = 1.dp)
    ) {
        Text(
            fontFamily = Montserrat, text = text, color = Color.White, fontSize = 12.sp,
            lineHeight = 18.sp, fontWeight = Medium,
        )
    }
}