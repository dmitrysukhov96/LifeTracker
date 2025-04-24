package com.dmitrysukhov.lifetracker.habits

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Habit
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.projects.ColorPicker
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import com.dmitrysukhov.lifetracker.utils.WhitePine

const val NEW_HABIT_SCREEN = "NewHabitScreen"

@Composable
fun NewHabitScreen(
    setTopBarState: (TopBarState) -> Unit,
    navController: NavHostController,
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf("") }
    var selectedGroupIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedNumberIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedColorInt by rememberSaveable { mutableIntStateOf(8) }

    LaunchedEffect(title, selectedGroupIndex, selectedNumberIndex, selectedColorInt) {
        setTopBarState(
            TopBarState(context.getString(R.string.new_habit)) {
                IconButton(onClick = {
                    val type =
                        if (selectedGroupIndex == 0) 0 else if (selectedNumberIndex == 0) 1 else 2
                    viewModel.addHabit(Habit(title = title, type = type, color = selectedColorInt))
                    navController.navigateUp()
                }) {
                    if (title.isNotBlank()) {
                        Icon(
                            painter = painterResource(R.drawable.tick),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = H1.copy(color = InverseColor),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) Text(
                        stringResource(R.string.new_habit),
                        style = H1,
                        color = InverseColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.habit_type),
            style = H2,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ToggleSelector(
            leftText = stringResource(R.string.checkbox),
            rightText = stringResource(R.string.number),
            selectedIndex = selectedGroupIndex,
            onSelectedChange = { selectedGroupIndex = it }
        )
        if (selectedGroupIndex == 1) {
            Spacer(modifier = Modifier.height(8.dp))
            ToggleSelector(
                leftText = stringResource(R.string.number_greater),
                rightText = stringResource(R.string.number_less),
                selectedIndex = selectedNumberIndex,
                onSelectedChange = { selectedNumberIndex = it }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.select_project_color),
            style = H2,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ColorPicker(
            selectedColorIndex = selectedColorInt,
            onColorSelected = { selectedColorInt = it }
        )
    }
}

@Composable
fun ToggleSelector(
    leftText: String, rightText: String, selectedIndex: Int,
    onSelectedChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = leftText,
            style = H2,
            color = if (selectedIndex == 0) PineColor else InverseColor,
            modifier = Modifier.padding(8.dp)
        )
        var checked by rememberSaveable { mutableStateOf(false) }
        val transition = updateTransition(targetState = checked, label = "switch")
        val knobOffset by transition.animateDp(label = "knobOffset") {
            if (it) 20.dp else 0.dp
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PineColor)
                .clickable {
                    checked = !checked
                    onSelectedChange(if (checked) 1 else 0)
                }
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(WhitePine)
            )
        }
        Text(
            text = rightText,
            style = H2,
            color = if (selectedIndex == 1) PineColor else InverseColor,
            modifier = Modifier.padding(8.dp)
        )
    }
}
