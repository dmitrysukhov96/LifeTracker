package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.todo.TodoListItem
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BoldText
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun ViewProjectScreen(setTopBarState: (TopBarState) -> Unit, navController: NavHostController) {
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState("Диплом") { //todo real project name
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White)
                }
            }
        )
    }
    val color = Color(0xFF669DE5)
    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Описание:", style = BoldText, color = color)
        Text(
            "написать диплом Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum",
            style = SimpleText,
            color = InverseColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text("Цель:", style = BoldText, color = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text("закончить универ", style = SimpleText, color = InverseColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = color.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.task),
                contentDescription = null, tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Задачи:", style = H2, color = InverseColor)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(modifier = Modifier.size(16.dp), onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Добавить задачу",
                    tint = Color(0xFF669DE5)
                )
            }
            Spacer(Modifier.width(20.dp))
        }
        TodoListItem(
            TodoItem(
                0, "Купить удлинитель, порошок", "", null,
                System.currentTimeMillis(), 2345, "", 456, false
            ), {}, false
        )
        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = color.copy(alpha = 0.5f))
    }

}

val VIEW_PROJECT_SCREEN = "view project screen"