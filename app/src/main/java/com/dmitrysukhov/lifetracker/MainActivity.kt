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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dmitrysukhov.lifetracker.habits.HABIT_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitScreen
import com.dmitrysukhov.lifetracker.projects.PROJECTS_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsScreen
import com.dmitrysukhov.lifetracker.todo.NEW_TASK_SCREEN
import com.dmitrysukhov.lifetracker.todo.NewTaskScreen
import com.dmitrysukhov.lifetracker.todo.TODOLIST_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoListScreen
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.TRACKER_SCREEN
import com.dmitrysukhov.lifetracker.tracker.TrackerScreen
import com.dmitrysukhov.lifetracker.turbo.TURBO_SCREEN
import com.dmitrysukhov.lifetracker.turbo.TurboScreen
import com.dmitrysukhov.lifetracker.utils.AccentColor
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BlackPine
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.MyApplicationTheme
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var topBarState by remember { mutableStateOf(TopBarState("LifeTracker")) }
                val setTopBarState: (TopBarState) -> Unit = { topBarState = it }
                var isTodo by rememberSaveable { mutableStateOf(false) }
                val todoViewModel: TodoViewModel = hiltViewModel()
                val navController = rememberNavController()
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(PineColor)
                ) {
                    SharedTransitionLayout {
                        Scaffold( //todo таки создать шторку
                            topBar = {
                                isTodo = navController.currentDestination?.route == TODOLIST_SCREEN
                                val canNavigateBack = navController.previousBackStackEntry != null
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = WindowInsets.systemBars.asPaddingValues()
                                                .calculateTopPadding()
                                        )
                                        .height(56.dp)
                                ) {
                                    if (canNavigateBack) {
                                        IconButton(
                                            onClick = { navController.popBackStack() },
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Назад", tint = WhitePine
                                            )
                                        }
                                    }
                                    Text(
                                        topBarState.title, fontFamily = Montserrat,
                                        fontSize = 20.sp, fontWeight = Bold,
                                        color = WhitePine,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                    Row(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 28.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        topBarState.topBarActions.invoke(this)
                                    }
                                }

                            },
                            bottomBar = {
                                if (isTodo) Row(
                                    Modifier
                                        .background(BgColor)
                                        .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                                        .background(PineColor)
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    var taskText by rememberSaveable { mutableStateOf("") }
                                    val style =
                                        TextStyle(fontWeight = Bold, fontFamily = Montserrat, color = Color.White)
                                    BasicTextField(
                                        textStyle = style,
                                        value = taskText, singleLine = true, cursorBrush = SolidColor(Color.White),
                                        onValueChange = { taskText = it },
                                        modifier = Modifier.padding(top = 16.dp, start = 24.dp).weight(1f),
                                        decorationBox = {
                                            it()
                                            if (taskText.isEmpty()) Text("Введите задачу...", color = Color.White)
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (taskText.isNotBlank()) {
//                                                    todoViewModel.addTask(taskText)
                                                    taskText = ""
                                                }
                                            }
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (taskText.isNotBlank()) {
                                                todoViewModel.addTask(taskText)
                                                taskText = ""
                                            }
                                        }
                                    ) { Text("Добавить", fontFamily = Montserrat) }

                                }
                            }, floatingActionButton = {
                                ActuallyFloatingActionButton { navController.navigate(TURBO_SCREEN) }
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
                                    composable(TODOLIST_SCREEN) {
                                        TodoListScreen(setTopBarState, navController, todoViewModel)
                                    }
                                    composable(TRACKER_SCREEN) { TrackerScreen() }
                                    composable(HABIT_SCREEN) { HabitScreen() }
                                    composable(PROJECTS_SCREEN) { ProjectsScreen() }
                                    composable(NEW_TASK_SCREEN) { NewTaskScreen(setTopBarState) }
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