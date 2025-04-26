package com.dmitrysukhov.lifetracker.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.daily.DAILY_PLANNER_SCREEN
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.util.Calendar

const val DASHBOARD_SCREEN = "dashboard_screen"

@Composable
fun DashboardScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    userName: String
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 4 -> stringResource(R.string.good_night)
        hour < 12 -> stringResource(R.string.good_morning)
        hour < 17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        setTopBarState(
            TopBarState(context.getString(R.string.app_name)) {
                IconButton({ navController.navigate(DAILY_PLANNER_SCREEN) }) {
                    Icon(Icons.Default.DateRange, null)
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(16.dp)
    ) { Text(text = greeting+if (userName.isNotBlank()) ", $userName!" else "!", style = H1) }
}