package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodoListScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: TodoViewModel
) {
    val todoList by viewModel.todoList.collectAsState()
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("LifeTracker") {
            IconButton({ navController.navigate(NEW_TASK_SCREEN) }) {
                Icon(
                    painterResource(R.drawable.plus),
                    contentDescription = null,
                    tint = Color.White
                )
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
                    onCheckedChange = { isChecked -> viewModel.updateTask(todoItem.copy(isDone = isChecked)) },
                    todoList.indexOf(todoItem) == 0 //todo real watching in tracker db
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TodoListItem(item: TodoItem, onCheckedChange: (Boolean) -> Unit, isRunning: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(if (item.isDone) R.drawable.checked else R.drawable.not_checked),
            contentDescription = null,
            modifier = Modifier
                .clickable { onCheckedChange(!item.isDone) }
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.text, textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    fontFamily = Montserrat,
                    fontSize = 14.sp, lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isDone) PineColor else InverseColor,
                    modifier = Modifier.weight(1f)
                )
                item.projectId?.let { //todo project name by id
                    ProjectTag(text = "Покупки", color = Color(0xFFFFA726))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item.durationMinutes?.let { duration -> DurationBadge(duration, isRunning) }
                if (item.reminderTime != null || item.repeatInterval != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        item.reminderTime?.let { time ->
                            Text(
                                text = formatTime(time),
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                painter = painterResource(R.drawable.bell),
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                            )
                        }

                        item.repeatInterval?.let {
                            Icon(
                                painter = painterResource(R.drawable.repeat),
                                contentDescription = null,
                                tint = PineColor,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

const val TODOLIST_SCREEN = "Todo List"

fun formatDuration(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
}

fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@Composable
fun DurationBadge(duration: Int, isRunning: Boolean) {
    val backgroundColor = if (isRunning) PineColor else BgColor
    val contentColor = if (isRunning) Color.White else PineColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .height(18.dp)
            .clip(CircleShape)
            .border(1.dp, PineColor, CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 6.dp)
    ) {
        Icon(
            painter = painterResource(if (isRunning) R.drawable.stop else R.drawable.play),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(8.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatDuration(duration),
            color = contentColor,
            fontSize = 12.sp, lineHeight = 10.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun ProjectTag(text: String, color: Color) {
    Box(
        Modifier
            .background(color, shape = RoundedCornerShape(52))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            fontFamily = Montserrat, text = text, color = Color.White, fontSize = 12.sp,
            lineHeight = 18.sp, fontWeight = FontWeight.Medium,
        )
    }
}
