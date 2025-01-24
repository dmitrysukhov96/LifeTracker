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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
                    LifeTrackerApp()
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
        val type = "Play"
        Row {
            Box(modifier = Modifier
                .clickable {

                }
                .clip(CircleShape)
                .background(PineColor)
                .size(50.dp)) {
                Image(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Run",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun LifeTrackerApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Todo,
        Screen.Tracker,
        Screen.Habits,
        Screen.Projects,
        Screen.Statistics
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.iconResourceId),
                                contentDescription = null
                            )
                        },
                        label = { Text(screen.route) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Todo.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Todo.route) { TodoListScreen(navController) }
            composable(Screen.DailyPlanning.route) { DailyPlanningScreen {  } }
            composable(Screen.Turbo.route) { TurboModeScreen {  } }
            composable(Screen.Tracker.route) { TrackerScreen() }
            composable(Screen.Habits.route) { HabitsScreen() }
            composable(Screen.Projects.route) { ProjectsScreen() }
            composable(Screen.Statistics.route) { StatisticsScreen() }
        }
    }
}

sealed class Screen(val route: String, val iconResourceId: Int) {
    object Todo : Screen("todo", R.drawable.ic_todo)
    object Tracker : Screen("tracker", R.drawable.ic_tracker)
    object Turbo : Screen("turbo", R.drawable.ic_tracker)
    object DailyPlanning : Screen("dp", R.drawable.ic_tracker)
    object Habits : Screen("habits", R.drawable.ic_habit)
    object Projects : Screen("projects", R.drawable.ic_projects)
    object Statistics : Screen("statistics", R.drawable.stats)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("New Task") },
            navigationIcon = {
                IconButton(onClick = { /* Handle back navigation */ }) {
                    Icon(painterResource(id = R.drawable.ic_chevron_left), contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Handle delete */ }) {
                    Icon(painterResource(id = R.drawable.trash), contentDescription = "Delete")
                }
            }
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        TaskOptionItem(
            icon = R.drawable.ic_calendar,
            text = "Дата/время"
        )

        TaskOptionItem(
            icon = R.drawable.ic_bell,
            text = "Добавить напоминание"
        )

        TaskOptionItem(
            icon = R.drawable.ic_habit,
            text = "Добавить повторение"
        )

        TaskOptionItem(
            icon = R.drawable.clock,
            text = "Добавить время на задачу"
        )
    }
}

@Composable
fun TaskOptionItem(icon: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    var selectedPeriod by remember { mutableStateOf("Week") }
    val periods = listOf("Day", "Week", "Month", "Year")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ChipGroup(
                    items = periods,
                    selectedItem = selectedPeriod,
                    onSelectedChanged = { selectedPeriod = it }
                )
            }

            item {
                StatisticsCard(
                    title = "Time Tracked",
                    value = "32h 15m",
                    change = "+2.5h",
                    positive = true
                )
            }

            item {
                ProductivityChart()
            }

            item {
                CategoryBreakdown()
            }

            item {
                TopProjects()
            }
        }
    }
}

@Composable
fun ChipGroup(
    items: List<String>,
    selectedItem: String,
    onSelectedChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onSelectedChanged(item) },
                label = { Text(item) }
            )
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    value: String,
    change: String,
    positive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
fun ProductivityChart() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Productivity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        }
    }
}

@Composable
fun CategoryBreakdown() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Add pie chart here
            CategoryProgressBars()
        }
    }
}

@Composable
fun CategoryProgressBars() {
    val categories = listOf(
        Triple("Work", 0.4f, MaterialTheme.colorScheme.primary),
        Triple("Study", 0.3f, MaterialTheme.colorScheme.secondary),
        Triple("Personal", 0.2f, MaterialTheme.colorScheme.tertiary),
        Triple("Other", 0.1f, MaterialTheme.colorScheme.error)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (name, progress, color) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name)
                    Text("${(progress * 100).toInt()}%")
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = color
                )
            }
        }
    }
}

@Composable
fun TopProjects() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top Projects",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectProgressItem("LifeTracker", "6h 30m", 0.8f)
                ProjectProgressItem("Study", "4h 15m", 0.6f)
                ProjectProgressItem("Exercise", "2h 45m", 0.4f)
            }
        }
    }
}

@Composable
fun ProjectProgressItem(
    name: String,
    time: String,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name)
            Text(time)
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

data class Habit(
    val id: String,
    val title: String,
    val streak: Int,
    val target: Int,
    val completed: Boolean,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit("1", "Morning Meditation", 7, 10, true, Color.Yellow),
                Habit("2", "Read 30 minutes", 3, 5, false, Color.Yellow),
                Habit("3", "Exercise", 12, 15, true, Color.Yellow),
                Habit("4", "Learn Kotlin", 5, 7, false, Color.Yellow)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                actions = {
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Handle new habit */ }) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HabitsSummary(habits)
            }

            items(habits) { habit ->
                HabitCard(
                    habit = habit,
                    onToggle = { /* Handle habit toggle */ }
                )
            }
        }
    }
}

@Composable
fun HabitsSummary(habits: List<Habit>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${habits.count { it.completed }}/${habits.size}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                CircularProgressIndicator(
                    progress = habits.count { it.completed }.toFloat() / habits.size,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${habit.streak} day streak",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = habit.streak.toFloat() / habit.target,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = habit.color
                )
            }

            Checkbox(
                checked = habit.completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = habit.color
                )
            )
        }
    }
}