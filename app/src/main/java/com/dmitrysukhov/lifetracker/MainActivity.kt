package com.dmitrysukhov.lifetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dmitrysukhov.lifetracker.habits.HABIT_SCREEN
import com.dmitrysukhov.lifetracker.habits.HabitScreen
import com.dmitrysukhov.lifetracker.habits.NEW_HABIT_SCREEN
import com.dmitrysukhov.lifetracker.habits.NewHabitScreen
import com.dmitrysukhov.lifetracker.projects.NEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.projects.NewProjectScreen
import com.dmitrysukhov.lifetracker.projects.PROJECTS_SCREEN
import com.dmitrysukhov.lifetracker.projects.ProjectsScreen
import com.dmitrysukhov.lifetracker.projects.ProjectsViewModel
import com.dmitrysukhov.lifetracker.projects.VIEW_PROJECT_SCREEN
import com.dmitrysukhov.lifetracker.projects.ViewProjectScreen
import com.dmitrysukhov.lifetracker.todo.NEW_TASK_SCREEN
import com.dmitrysukhov.lifetracker.todo.NewTaskScreen
import com.dmitrysukhov.lifetracker.todo.TODOLIST_SCREEN
import com.dmitrysukhov.lifetracker.todo.TodoListScreen
import com.dmitrysukhov.lifetracker.todo.TodoViewModel
import com.dmitrysukhov.lifetracker.tracker.TRACKER_SCREEN
import com.dmitrysukhov.lifetracker.tracker.TrackerScreen
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.MyApplicationTheme
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = false
            var topBarState by remember { mutableStateOf(TopBarState(context.getString(R.string.app_name))) }
            LaunchedEffect(topBarState.color) {
                systemUiController.setStatusBarColor(topBarState.color, darkIcons = useDarkIcons)
                systemUiController.setNavigationBarColor(topBarState.color, darkIcons = useDarkIcons)
            }
            MyApplicationTheme {
                // Name dialog logic
                val sharedPref = context.getSharedPreferences("user_prefs", MODE_PRIVATE)
                var showNameDialog by rememberSaveable {
                    mutableStateOf(
                        !sharedPref.getBoolean(
                            "dont_ask_name",
                            false
                        )
                    )
                }
                var userName by rememberSaveable { mutableStateOf("") }
                if (showNameDialog) {
                    AlertDialog(
                        onDismissRequest = { showNameDialog = false },
                        title = { Text(text = stringResource(R.string.dialog_ask_name)) },
                        text = {
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                placeholder = { Text(text = stringResource(R.string.dialog_ask_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    sharedPref.edit {
                                        putString("user_name", userName.trim())
                                            .putBoolean("dont_ask_name", true)
                                    }
                                    showNameDialog = false
                                },
                                enabled = userName.isNotBlank()
                            ) {
                                Text(text = stringResource(R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    sharedPref.edit {
                                        putBoolean("dont_ask_name", true)
                                    }
                                    showNameDialog = false
                                }
                            ) {
                                Text(text = stringResource(R.string.dialog_dont_ask_again))
                            }
                        }
                    )
                }
                val setTopBarState: (TopBarState) -> Unit = { topBarState = it }
                val todoViewModel: TodoViewModel = hiltViewModel()
                val projectViewModel: ProjectsViewModel = hiltViewModel()
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route ?: ""
                val drawerMenuDestinations = listOf(
                    Destination(stringResource(R.string.todo_list), TODOLIST_SCREEN, painterResource(R.drawable.spisok)),
                    Destination(stringResource(R.string.tracker), TRACKER_SCREEN, painterResource(R.drawable.tracker)),
                    Destination(stringResource(R.string.habits), HABIT_SCREEN, painterResource(R.drawable.habits)),
                    Destination(stringResource(R.string.projects), PROJECTS_SCREEN, painterResource(R.drawable.projects))
                )
                val isRootScreen = drawerMenuDestinations.any { it.route == currentDestination }
                val isTodo = currentDestination == TODOLIST_SCREEN
                ModalNavigationDrawer(
                    drawerState = drawerState, gesturesEnabled = isRootScreen, drawerContent = {
                        ModalDrawerSheet(drawerContainerColor = BgColor) {
                            Text(
                                text = stringResource(R.string.app_title), fontFamily = Montserrat,
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
                                        selectedTextColor = topBarState.color,
                                        selectedContainerColor = topBarState.color.copy(0.1f),
                                        unselectedTextColor = InverseColor,
                                        selectedIconColor = topBarState.color
                                    ), shape = RectangleShape,
                                    selected = currentDestination == destination.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(destination.route) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                    content = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(topBarState.color)
                        ) {
//                            SharedTransitionLayout {
                            Scaffold(
                                floatingActionButton = { ActuallyFloatingActionButton { } },
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
                                        if (isRootScreen) {
                                            IconButton(
                                                onClick = { scope.launch { drawerState.open() } },
                                                modifier = Modifier
                                                    .padding(start = 10.dp)
                                                    .align(Alignment.CenterStart)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = stringResource(R.string.menu), tint = WhitePine
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { navController.popBackStack() },
                                                modifier = Modifier
                                                    .padding(start = 10.dp)
                                                    .align(Alignment.CenterStart)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.back), tint = WhitePine
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
                                            .background(topBarState.color)
                                            .fillMaxWidth()
                                            .height(72.dp)
                                            .padding(horizontal = 24.dp)
                                            .align(Alignment.BottomCenter),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        var taskText by rememberSaveable { mutableStateOf("") }
                                        val style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = Montserrat, color = Color.White
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            BasicTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp),
                                                textStyle = style,
                                                value = taskText,
                                                singleLine = true,
                                                cursorBrush = SolidColor(Color.White),
                                                onValueChange = { taskText = it },
                                                decorationBox = { innerTextField ->
                                                    Box(contentAlignment = Alignment.CenterStart) {
                                                        innerTextField()
                                                        if (taskText.isEmpty()) {
                                                            Text(
                                                                stringResource(R.string.enter_task_hint),
                                                                color = Color.White,
                                                                style = H2
                                                            )
                                                        }
                                                    }
                                                },

                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    if (taskText.isNotBlank()) {
                                                        todoViewModel.addTask(taskText)
                                                        taskText = ""
                                                    }
                                                })
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            HorizontalDivider(color = Color.White)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        IconButton({
                                            if (taskText.isNotBlank()) {
                                                todoViewModel.addTask(taskText)
                                                taskText = ""
                                            }
                                        }, modifier = Modifier.size(22.dp)) {
                                            Image(
                                                painter = painterResource(R.drawable.send),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            ) { padding ->
                                Box(Modifier.fillMaxSize()) {
                                    NavHost(
                                        navController = navController,
                                        startDestination = TODOLIST_SCREEN, modifier = Modifier
                                            .background(topBarState.color)
                                            .padding(padding)
                                            .clip(
                                                RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                                            )
                                    ) {
                                        composable(TODOLIST_SCREEN) {
                                            TodoListScreen(
                                                setTopBarState, navController, todoViewModel
                                            )
                                        }
                                        composable(VIEW_PROJECT_SCREEN) { ViewProjectScreen(setTopBarState, projectViewModel, navController) }
                                        composable(TRACKER_SCREEN) { TrackerScreen(setTopBarState, navController = navController) }
                                        composable(HABIT_SCREEN) {
                                            HabitScreen(
                                                setTopBarState,
                                                navController
                                            )
                                        }
                                        composable(NEW_HABIT_SCREEN) {
                                            NewHabitScreen(
                                                setTopBarState,
                                                navController
                                            )
                                        }
                                        composable(PROJECTS_SCREEN) {
                                            ProjectsScreen(setTopBarState, navController, projectViewModel)
                                        }
                                        composable(NEW_TASK_SCREEN) {
                                            NewTaskScreen(setTopBarState, todoViewModel, navController)
                                        }
                                        composable(NEW_PROJECT_SCREEN) {
                                            NewProjectScreen(setTopBarState, navController, projectViewModel)
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