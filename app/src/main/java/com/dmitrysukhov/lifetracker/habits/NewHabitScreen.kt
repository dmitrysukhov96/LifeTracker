package com.dmitrysukhov.lifetracker.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Habit
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.common.ui.ColorPicker
import com.dmitrysukhov.lifetracker.common.ui.SubtitleWithIcon
import com.dmitrysukhov.lifetracker.common.ui.ToggleSelector
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

const val NEW_HABIT_SCREEN = "NewHabitScreen"

@Composable
fun NewHabitScreen(
    setTopBarState: (TopBarState) -> Unit, navController: NavHostController,
    viewModel: HabitsViewModel
) {
    val context = LocalContext.current
    val selected = viewModel.selectedHabit
    var title by rememberSaveable { mutableStateOf(selected?.title ?: "") }
    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(selected?.type ?: 0) }
    var selectedColorInt by rememberSaveable {
        mutableIntStateOf(
            selected?.color ?: PineColor.toArgb()
        )
    }
    setTopBarState(
        TopBarState(
            if (selected == null) context.getString(R.string.new_habit)
            else context.getString(R.string.edit_habit), screen = NEW_HABIT_SCREEN
        ) {
            Row {
                if (selected != null) {
                    IconButton(onClick = {
                        viewModel.deleteHabit(selected.id)
                        navController.navigateUp()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = {
                    val habit = Habit(
                        id = selected?.id ?: 0, title = title, type = selectedTypeIndex,
                        color = selectedColorInt
                    )
                    if (selected == null) viewModel.addHabit(habit)
                    else viewModel.updateHabit(habit)
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
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        BasicTextField(
            value = title, cursorBrush = SolidColor(PineColor),
            onValueChange = { title = it },
            textStyle = H1.copy(color = InverseColor),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) Text(
                        stringResource(R.string.new_habit), style = H1, color = PineColor.copy(0.5f)
                    )
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        if (selected == null) {
            SubtitleWithIcon(R.string.habit_type, R.drawable.switcher, PineColor)
            Spacer(Modifier.height(8.dp))
            ToggleSelector(
                leftText = stringResource(R.string.checkbox),
                rightText = stringResource(R.string.number),
                selectedIndex = selectedTypeIndex,
                onSelectedChange = { selectedTypeIndex = it }
            )
            Spacer(Modifier.height(16.dp))
        }

        SubtitleWithIcon(R.string.select_color, R.drawable.palette, PineColor)
        Spacer(Modifier.height(8.dp))
        ColorPicker(
            selectedColorInt = selectedColorInt, onColorSelected = { selectedColorInt = it })
    }
}