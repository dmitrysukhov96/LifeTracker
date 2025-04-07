package com.dmitrysukhov.lifetracker


import TodoViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dmitrysukhov.lifetracker.todo.NewTaskScreen
import com.dmitrysukhov.lifetracker.todo.TodoListScreen

import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NavGraph(
    navController: NavHostController,
    setTopBarState: (TopBarState) -> Unit,
    viewModel: TodoViewModel
) {
    NavHost(navController, startDestination = "TodoListScreen") {
        composable("TodoListScreen") {
            TodoListScreen(setTopBarState, viewModel, navController)
        }
        composable("NewTaskScreen") {
            NewTaskScreen(setTopBarState, viewModel, navController)
        }
    }
}