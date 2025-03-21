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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dmitrysukhov.lifetracker.habits.HABIT_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitScreen
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.projects.NewProjectScreen
import com.dmitrysukhov.lifetracker.projects.PROJECTS_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsScreen
import com.dmitrysukhov.lifetracker.todo.NEW_TASK_SCREEN
import com.dmitrysukhov.lifetracker.todo.NewTaskScreen
import com.dmitrysukhov.lifetracker.todo.TODOLIST_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoListScreen
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.TRACKER_SCREEN
import com.dmitrysukhov.lifetracker.tracker.TrackerScreen
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.MyApplicationTheme
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                var topBarState by remember { mutableStateOf(TopBarState("LifeTracker")) }
                val setTopBarState: (TopBarState) -> Unit = { topBarState = it }
                val viewModel: TodoViewModel = hiltViewModel()
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route ?: ""
                val drawerMenuDestinations = listOf(
                    Destination("Список дел", TODOLIST_SCREEN, painterResource(R.drawable.spisok)),
                    Destination("Трекер", TRACKER_SCREEN, painterResource(R.drawable.tracker)),
                    Destination("Привычки", HABIT_SCREEN, painterResource(R.drawable.habits)),
                    Destination("Проекты", PROJECTS_SCREEN, painterResource(R.drawable.projects))
                )
                val isRootScreen = drawerMenuDestinations.any { it.route == currentDestination }
                val isTodo = currentDestination == TODOLIST_SCREEN
                ModalNavigationDrawer(
                    drawerState = drawerState, gesturesEnabled = isRootScreen, drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = BgColor) {
                            Text(
                                text = "LifeTracker", fontFamily = Montserrat,
                                fontSize = 20.sp, color = InverseColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            drawerMenuDestinations.forEach { destination ->
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            destination.title, fontFamily = Montserrat,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            destination.icon, destination.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }, colors = NavigationDrawerItemDefaults.colors(
                                        selectedTextColor = PineColor,
                                        selectedContainerColor = PineColor.copy(0.1f),
                                        unselectedTextColor = InverseColor,
                                        selectedIconColor = PineColor
                                    ), shape = RectangleShape,
                                    selected = currentDestination == destination.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(destination.route)
                                        //todo убрать возможность навигироваться назад
                                    }
                                )
                            }
                        }
                    },
                    content = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(PineColor)
                        ) {
//                            SharedTransitionLayout {
                            Scaffold(
                                floatingActionButton = { ActuallyFloatingActionButton { } },
                                topBar = {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = WindowInsets.systemBars.asPaddingValues()
                                                    .calculateTopPadding()
                                            )
                                            .height(56.dp)
                                    ) {
                                        if (isRootScreen) {
                                            IconButton(
                                                onClick = { scope.launch { drawerState.open() } },
                                                modifier = Modifier.padding(start = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Меню", tint = WhitePine
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { navController.popBackStack() },
                                                modifier = Modifier.padding(start = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Назад", tint = WhitePine
                                                )
                                            }
                                        }
                                        Text(
                                            topBarState.title, fontFamily = Montserrat,
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                            color = WhitePine,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                        Row(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 28.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) { topBarState.topBarActions.invoke(this) }
                                    }
                                }, bottomBar = {
                                    if (isTodo) Row(
                                        Modifier
                                            .background(BgColor)
                                            .clip(
                                                RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                                            )
                                            .background(PineColor)
                                            .fillMaxWidth()
                                            .height(72.dp)
                                            .align(Alignment.BottomCenter)
                                    ) {
                                        var taskText by rememberSaveable { mutableStateOf("") }
                                        val style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Montserrat, color = Color.White
                                        )
                                        BasicTextField(
                                            textStyle = style, value = taskText, singleLine = true,
                                            cursorBrush = SolidColor(Color.White),
                                            onValueChange = { taskText = it },
                                            modifier = Modifier
                                                .padding(top = 16.dp, start = 24.dp)
                                                .weight(1f), decorationBox = {
                                                it()
                                                if (taskText.isEmpty()) Text(
                                                    "Введите задачу...", color = Color.White
                                                )
                                            }, keyboardOptions = KeyboardOptions.Default.copy(
                                                imeAction = ImeAction.Done
                                            ), keyboardActions = KeyboardActions(onDone = {
                                                if (taskText.isNotBlank()) taskText = ""
                                            })
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (taskText.isNotBlank()) {
                                                    viewModel.addTask(taskText)
                                                    taskText = ""
                                                }
                                            }
                                        ) { Text("Добавить", fontFamily = Montserrat) }
                                    }
                                }
                            ) { padding ->
                                Box(Modifier.fillMaxSize()) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = TODOLIST_SCREEN, modifier = Modifier
                                            .background(PineColor)
                                            .padding(padding)
                                            .clip(
                                                RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                                            )
                                    ) {
                                        composable(TODOLIST_SCREEN) {
                                            TodoListScreen(
                                                setTopBarState, navController, viewModel
                                            )
                                        }
                                        composable(TRACKER_SCREEN) { TrackerScreen(setTopBarState) }
                                        composable(HABIT_SCREEN) { HabitScreen(setTopBarState) }
                                        composable(PROJECTS_SCREEN) {
                                            ProjectsScreen(setTopBarState, navController)
                                        }
                                        composable(NEW_TASK_SCREEN) { NewTaskScreen(setTopBarState) }
                                        composable(NEW_PROJECT_SCREEN) {
                                            NewProjectScreen(setTopBarState)
                                        }
                                    }
                                }
                            }
                        }
//                        }
                    }
                )
            }
        }
    }
}

data class Destination(val title: String, val route: String, val icon: Painter)

@Composable
fun ActuallyFloatingActionButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "")
    val offsetY by transition.animateFloat(
        initialValue = 0f, targetValue = 5f, animationSpec = infiniteRepeatable(
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
            .background(color = Color(0xFF33BA78), shape = RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically
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

//const val FAB_EXPLODE_BOUNDS_KEY = "FAB_EXPLODE_BOUNDS_KEY"

//@Composable
//fun TimeTracker(padding: PaddingValues) {
//    Row(
//        Modifier
//            .fillMaxWidth()
//            .padding(top = padding.calculateTopPadding() + 12.dp)
//            .padding(horizontal = 24.dp)
//            .height(64.dp)
//            .clip(RoundedCornerShape(100.dp))
//            .background(AccentColor)
//            .padding(horizontal = 24.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Column {
//            Text(
//                "Task123", color = BlackPine, fontWeight = Bold,
//                fontFamily = Montserrat, fontSize = 18.sp
//            )
//            Text(
//                "Project456", color = BlackPine, fontWeight = Bold,
//                fontFamily = Montserrat, fontSize = 14.sp
//            )
//        }
//        Text(
//            "00:23:57", color = BlackPine, fontWeight = Bold,
//            fontFamily = Montserrat, fontSize = 20.sp
//        )
//        var type by rememberSaveable { mutableStateOf("Play") }
//
//        Row {
//            Box(
//                modifier = Modifier
//                    .clickable {
//                        type = if (type == "Play") "Stop" else "Play"
//                    }
//                    .clip(CircleShape)
//                    .background(PineColor)
//                    .size(50.dp)
//            ) {
//                Image(
//                    painter = painterResource(
//                        id = if (type == "Play") R.drawable.play else R.drawable.stop
//                    ),
//                    contentDescription = if (type == "Play") "Run" else "Stop",
//                    contentScale = ContentScale.FillBounds,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//        }
//    }
//}