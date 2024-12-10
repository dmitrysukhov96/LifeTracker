package com.dmitrysukhov.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val items = listOf(
                    BottomNavigationItem(
                        title = TRACKER_SCREEN,
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,

                        ),
                    BottomNavigationItem(
                        title = TODOLIST_SCREEN,
                        selectedIcon = Icons.Filled.DateRange,
                        unselectedIcon = Icons.Outlined.DateRange,
                        hasNews = false,
                        badgeCount = 45

                    ),

                    BottomNavigationItem(
                        title = SETTINGS_SCREEN,
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        hasNews = true,

                        ),
                )
                val context = LocalContext.current
                val todoDao = AppDatabase.getDatabase(context).todoDao()
                val viewModel: TodoViewModel = viewModel(factory = TodoViewModelFactory(todoDao))

                var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                Scaffold(bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(selected = selectedItemIndex == index, onClick = {
                                selectedItemIndex = index
                                navController.navigate(item.title)
                            },
                                label = { Text(text = item.title) },
                                alwaysShowLabel = false,
                                icon = {
                                BadgedBox(badge = {
                                    if (item.badgeCount != null)
                                        Badge { Text(text = item.badgeCount.toString()) }
                                    else if (item.hasNews) Badge()
                                }) {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            })
                        }
                    }
                }) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = TRACKER_SCREEN,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(TODOLIST_SCREEN) { TodoListScreen(viewModel) }
                        composable(SETTINGS_SCREEN) { SettingsScreen(navController) }
                        composable(TRACKER_SCREEN) { TrackerScreen(navController) }
                    }
                }
            }
        }
    }
}