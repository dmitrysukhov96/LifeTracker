package com.dmitrysukhov.lifetracker.daily
//
//import android.content.Context
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.navigation.NavHostController
//import com.dmitrysukhov.lifetracker.R
//import com.dmitrysukhov.lifetracker.utils.AccentColor
//import com.dmitrysukhov.lifetracker.utils.H1
//import com.dmitrysukhov.lifetracker.utils.PineColor
//import com.dmitrysukhov.lifetracker.utils.SimpleText
//import com.dmitrysukhov.lifetracker.utils.TopBarState
//import java.util.Calendar
//
//const val DAILY_PLANNER_SCREEN = "daily_planner_screen"
//
//@Composable
//fun DailyPlannerScreen(
//    setTopBarState: (TopBarState) -> Unit,
//    navController: NavHostController,
//    viewModel: DailyPlannerViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
//    val selectedTasks = remember { mutableStateMapOf<Long, Boolean>() }
//    val userName = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        .getString("user_name", "")
//
//    LaunchedEffect(Unit) {
//        setTopBarState(TopBarState(context.getString(R.string.daily_planner)))
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(PineColor, AccentColor)
//                )
//            )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//            val greeting = when {
//                hour < 4 -> stringResource(R.string.good_night)
//                hour < 12 -> stringResource(R.string.good_morning)
//                hour < 17 -> stringResource(R.string.good_afternoon)
//                else -> stringResource(R.string.good_evening)
//            }
//            Text(
//                text = greeting + if (!userName.isNullOrBlank()) ", $userName!" else "!",
//                style = H1, color = Color.White, modifier = Modifier.padding(top = 16.dp)
//            )
//            Text(
//                text = stringResource(R.string.select_tasks_for_today), style = SimpleText,
//                color = Color.White, modifier = Modifier.padding(top = 8.dp)
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            LazyColumn {
//                items(tasks.size) { index ->
//                    val task = tasks[index]
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(8.dp))
//                            .clickable {
//                                selectedTasks[task.id] = !(selectedTasks[task.id] ?: false)
//                            }
//                            .background(if (selectedTasks[task.id] == true) AccentColor else Color.Transparent)
//                            .padding(vertical = 8.dp, horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column {
//                            Text(text = task.text, style = SimpleText, color = Color.White)
////                            Text(text = projectTag, style = SimpleText, color = Color.Gray) // Отображение тега проекта
//                        }
//                        if (selectedTasks[task.id] == true) {
//                            Icon(
//                                painter = painterResource(R.drawable.tick),
//                                contentDescription = null, tint = Color.White,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        FloatingActionButton(
//            onClick = { /* Переход на следующий экран */ },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp),
//            shape = CircleShape,
//            containerColor = Color.White
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                contentDescription = null,
//                tint = PineColor
//            )
//        }
//    }
//}