
package com.dmitrysukhov.lifetracker.android.ui

import com.dmitrysukhov.lifetracker.TodoListScreen
import com.dmitrysukhov.lifetracker.TodoViewModel
import com.dmitrysukhov.lifetracker.TodoViewModelFactory



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmitrysukhov.lifetracker.android.data.AppDatabase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Получение контекста через LocalContext
            val context = LocalContext.current

            // Создание экземпляра DAO и ViewModel
            val todoDao = AppDatabase.getDatabase(context).todoDao()
            val viewModel: TodoViewModel = viewModel(factory = TodoViewModelFactory(todoDao))

            // Отображение экрана
            TodoListScreen(viewModel)
        }
    }
}



