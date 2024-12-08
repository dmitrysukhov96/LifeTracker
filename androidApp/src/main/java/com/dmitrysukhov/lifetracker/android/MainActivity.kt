package com.dmitrysukhov.lifetracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val items = listOf(
                    BottomNavigationItem(
                        title = "TimeTracker",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false,

                        ),
                    BottomNavigationItem(
                        title = "TODO",
                        selectedIcon = Icons.Filled.DateRange,
                        unselectedIcon = Icons.Outlined.DateRange,
                        hasNews = false,
                        badgeCount = 45

                    ),

                    BottomNavigationItem(
                        title = "Settings",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        hasNews = true,

                        ),
                )
                var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                Scaffold(bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(selected = selectedItemIndex == index, onClick = {
                                selectedItemIndex = index
//                                navController.navigate(item.title)
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
                }) {
                    //todo надо сделать тут нав хост и 3 экрана
                }
            }
        }
    }
}

//.
//Timofey
//сава тест 2
// Kostia