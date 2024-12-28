package com.dmitrysukhov.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
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
                    Scaffold(
                        topBar = {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding()
                                    )
                                    .height(56.dp)
                            ) {
                                Text(
                                    "LifeTracker", fontFamily = Montserrat,
                                    fontSize = 20.sp, fontWeight = Bold,
                                    color = WhitePine,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                        },
                        bottomBar = {
                            var selectedItem by rememberSaveable { mutableStateOf(MAIN_SCREEN) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                                    .height(89.dp)
                                    .background(PineColor)
                                    .padding(horizontal = 50.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Icon(
                                    painter =  painterResource (R.drawable.home),
                                    contentDescription = MAIN_SCREEN,
                                    tint = if (selectedItem == MAIN_SCREEN) AccentColor else Color.White,
                                    modifier = Modifier.clickable {
                                        selectedItem = MAIN_SCREEN
                                        navController.navigate(MAIN_SCREEN)
                                    }
                                )
                                Icon(
                                    painter =  painterResource (R.drawable.strekla),
                                    contentDescription = HABIT_SCREEN,
                                    tint = if (selectedItem == HABIT_SCREEN) AccentColor else Color.White,
                                    modifier = Modifier.clickable {
                                        selectedItem = HABIT_SCREEN
                                        navController.navigate(HABIT_SCREEN)
                                    }
                                )
                                Icon(
                                    painter = painterResource (R.drawable.spisok),
                                    contentDescription = TODOLIST_SCREEN,
                                    tint = if (selectedItem == TODOLIST_SCREEN) AccentColor else Color.White,
                                    modifier = Modifier.clickable {
                                        selectedItem = TODOLIST_SCREEN
                                        navController.navigate(TODOLIST_SCREEN)
                                    }
                                )
                                Icon(
                                    painter = painterResource (R.drawable.stat),
                                    contentDescription = TRACKER_SCREEN,
                                    tint = if (selectedItem == TRACKER_SCREEN) AccentColor else Color.White,
                                    modifier = Modifier.clickable {
                                        selectedItem = TRACKER_SCREEN
                                        navController.navigate(TRACKER_SCREEN)
                                    }
                                )
                            }
                        },
                        floatingActionButton = { ActuallyFloatingActionButton({ /*todo click*/ }) }
                    ) { padding ->
                        Box(Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = MAIN_SCREEN,
                                modifier = Modifier
                                    .background(PineColor)
                                    .padding(padding)
                                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                            ) {
                                composable(MAIN_SCREEN) { MainScreen(navController) }
                                composable(HABIT_SCREEN) { HabitScreen(navController) }
                                composable(TODOLIST_SCREEN) { TodoListScreen(navController) }
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
                                        "Task123", color = BlackPine, fontWeight = Bold,
                                        fontFamily = Montserrat, fontSize = 18.sp
                                    )
                                    Text(
                                        "Project456", color = BlackPine, fontWeight = Bold,
                                        fontFamily = Montserrat, fontSize = 14.sp
                                    )
                                }
                                Text(
                                    "00:23:57", color = BlackPine, fontWeight = Bold,
                                    fontFamily = Montserrat, fontSize = 20.sp
                                )
                                Row {
                                    Box(modifier = Modifier.size(50.dp)) {
                                        Image(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Run",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.align(Alignment.Center)
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

@Composable
fun ActuallyFloatingActionButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "")
    val offsetY by transition.animateFloat(
        initialValue = 0f, targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Row(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .height(56.dp)
            .width(86.dp)
            .clip(RoundedCornerShape(50.dp))
            .shadow(2.dp)
            .background(
                color = Color(0xFF33BA78),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.lightning),
            contentDescription = null, modifier = Modifier.size(16.dp, 22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "GO!",
            fontSize = 18.sp, fontStyle = Italic,
            color = Color.White, fontFamily = Montserrat,
            fontWeight = ExtraBold
        )
    }
}