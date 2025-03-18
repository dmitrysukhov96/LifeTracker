package com.dmitrysukhov.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dmitrysukhov.lifetracker.habits.HABIT_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitScreen
import com.dmitrysukhov.lifetracker.projects.PROJECTS_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsScreen
import com.dmitrysukhov.lifetracker.todo.ADD_TASK_SCREEN
import com.dmitrysukhov.lifetracker.todo.AddTaskScreen
import com.dmitrysukhov.lifetracker.todo.TODOLIST_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoListScreen
import com.dmitrysukhov.lifetracker.tracker.TRACKER_SCREEN
import com.dmitrysukhov.lifetracker.tracker.TrackerScreen
import com.dmitrysukhov.lifetracker.turbo.TURBO_SCREEN
import com.dmitrysukhov.lifetracker.turbo.TurboScreen
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.DarkerPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.MyApplicationTheme
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //todo переехать на ксп
        setContent {
            MyApplicationTheme {
                var topBarState by remember { mutableStateOf(TopBarState()) }
                val setTopBarState: (TopBarState) -> Unit = { topBarState = it }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PineColor)
                ) {
                    val navController = rememberNavController()
                    SharedTransitionLayout {
                        Scaffold( //todo таки создать шторку
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
                                        topBarState.title, fontFamily = Montserrat,
                                        fontSize = 20.sp, fontWeight = Bold,
                                        color = WhitePine,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                    topBarState.topBarActions
                                }

                            },
                            bottomBar = {
                                //todo реклама???
                            },
                            floatingActionButton = {
                                ActuallyFloatingActionButton({ navController.navigate(TURBO_SCREEN) })
                            }
                        ) { padding ->
                            Box(Modifier.fillMaxSize()) {
                                NavHost(
                                    navController = navController,
                                    startDestination = NEW_TASK_SCREEN,
                                    modifier = Modifier
                                        .background(PineColor)
                                        .padding(padding)
                                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                                ) {
                                    composable(TODOLIST_SCREEN) { TodoListScreen(setTopBarState, navController) }
                                    composable(TRACKER_SCREEN) { TrackerScreen() }
                                    composable(ADD_TASK_SCREEN) { AddTaskScreen() }
                                    composable(HABIT_SCREEN) { HabitScreen() }
                                    composable(PROJECTS_SCREEN) { ProjectsScreen() }
                                    composable(NEW_TASK_SCREEN) { NewTaskScreen() }
                                    composable(TURBO_SCREEN) { TurboScreen(this) }
                                }
//                                TimeTracker(padding)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarItem(iconRes: Int, text: String, isSelected: Boolean, onSelect: () -> Unit) {
    val color = if (isSelected) WhitePine else DarkerPine
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.clickable { onSelect() }
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = color)
        Text(text, fontSize = 12.sp, fontWeight = SemiBold, fontFamily = Montserrat, color = color)
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

const val FAB_EXPLODE_BOUNDS_KEY = "FAB_EXPLODE_BOUNDS_KEY"

@Composable
fun TimeTracker(padding: PaddingValues) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = padding.calculateTopPadding() + 12.dp)
            .padding(horizontal = 24.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(AccentColor)
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
        var type by rememberSaveable { mutableStateOf("Play") }

        Row {
            Box(
                modifier = Modifier
                    .clickable {
                        type = if (type == "Play") "Stop" else "Play"
                    }
                    .clip(CircleShape)
                    .background(PineColor)
                    .size(50.dp)
            ) {
                Image(
                    painter = painterResource(
                        id = if (type == "Play") R.drawable.play else R.drawable.stop
                    ),
                    contentDescription = if (type == "Play") "Run" else "Stop",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}