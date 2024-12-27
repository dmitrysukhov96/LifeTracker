package com.dmitrysukhov.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PineColor)
                ) {
                    val navController = rememberNavController()
                    val items = listOf(
                        BottomNavigationItem(
                            title = TRACKER_SCREEN,
                            selectedIcon = Icons.Rounded.Home,
                            unselectedIcon = Icons.Rounded.Home,
                        ),
                        BottomNavigationItem(
                            title = TODOLIST_SCREEN,
                            selectedIcon = Icons.Rounded.DateRange,
                            unselectedIcon = Icons.Rounded.DateRange,
                        ),

                        BottomNavigationItem(
                            title = SETTINGS_SCREEN,
                            selectedIcon = Icons.Rounded.Face,
                            unselectedIcon = Icons.Rounded.Face,
                        ),
                        BottomNavigationItem(
                            title = SETTINGS_SCREEN,
                            selectedIcon = Icons.Rounded.Settings,
                            unselectedIcon = Icons.Rounded.Settings,
                        )
                    )
                    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
                    Scaffold(
                        topBar = {
                            Row(
                                Modifier
                                    .background(PineColor)
                                    .fillMaxWidth()
                                    .padding(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding()
                                    )
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "LifeTracker", style = PoppinsBold,
                                    fontSize = 20.sp, color = WhitePine
                                )
                            }
                        },
                        bottomBar = {
                            NavigationBar(containerColor = BgColor, contentColor = Color.Red) {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(selected = selectedItemIndex == index,
                                        onClick = {
                                            selectedItemIndex = index
                                            navController.navigate(item.title)
                                        },
                                        alwaysShowLabel = false,
                                        icon = {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = ""
                                            )
                                        })
                                }
                            }
                        },
                        floatingActionButton = {
                            Row(
                                modifier = Modifier
                                    .height(56.dp)
                                    .width(86.dp)
                                    .background(
                                        color = Color(0xFF33BA78),
                                        shape = RoundedCornerShape(50.dp)
                                    )

                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {  }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.molni),
                                    contentDescription = null,
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GO!",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                            fontWeight = FontWeight.W700
                                )
                            }
                        }

                    ) { padding ->
                        Box(Modifier.fillMaxSize()) {

                            NavHost(
                                navController = navController,
                                startDestination = TRACKER_SCREEN,
                                modifier = Modifier
                                    .background(PineColor)
                                    .padding(padding)
                                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                            ) {
                                composable(TODOLIST_SCREEN) { TodoListScreen(navController) }
                                composable(SETTINGS_SCREEN) { SettingsScreen(navController) }
                                composable(TRACKER_SCREEN) { TrackerScreen(navController) }
                            }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = padding.calculateBottomPadding() + 8.dp)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(AccentColor)
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Task123", color = BlackPine,
                                        style = PoppinsBold, fontSize = 18.sp
                                    )
                                    Text(
                                        "Project456", color = BlackPine,
                                        style = PoppinsBold, fontSize = 14.sp
                                    )
                                }
                                Text(
                                    "00:23:57", color = BlackPine,
                                    style = PoppinsBold, fontSize = 20.sp
                                )
                                Row {
                                    Button(onClick = {}, modifier = Modifier.width(50.dp)) {
                                        Image(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Run",
                                            contentScale = ContentScale.FillBounds
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(onClick = {}, modifier = Modifier.width(50.dp)) {
                                        Image(
                                            Icons.Rounded.ArrowDropDown,
                                            contentDescription = "Change project",
                                            contentScale = ContentScale.FillBounds
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}