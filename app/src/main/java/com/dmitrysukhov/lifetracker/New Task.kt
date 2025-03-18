package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.WhitePine

@Composable
fun NewTaskScreen() {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhitePine)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок", fontWeight = FontWeight.Bold) },
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
        TaskOption("Дата/время")
        TaskOption("Проект", showIcon = true)
        TaskOption("Добавить напоминание")
        TaskOption("Добавить повторение")
        TaskOption("Добавить время на задачу")
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            
            .background(
                Color(0xFF2ECC71),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { }) {
            Icon(
                painterResource(id = android.R.drawable.ic_menu_revert),
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text("New Task", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = {}) {
            Icon(
                painterResource(id = android.R.drawable.ic_menu_delete),
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TaskOption(text: String, showIcon: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = android.R.drawable.ic_menu_agenda),
                contentDescription = null,
                tint = Color(0xFF2ECC71)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        if (showIcon) {
            Icon(
                painterResource(id = android.R.drawable.arrow_down_float),
                contentDescription = "Expand",
                tint = PineColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNewTaskScreen() {
    NewTaskScreen()
}

val NEW_TASK_SCREEN = "NewTaskScreen"
