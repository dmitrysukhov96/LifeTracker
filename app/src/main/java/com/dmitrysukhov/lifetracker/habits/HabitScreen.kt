package com.dmitrysukhov.lifetracker.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.Montserrat

@Composable
fun HabitScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(BgColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Habits", fontFamily = Montserrat)
    }
}

const val HABIT_SCREEN = "Habits"

