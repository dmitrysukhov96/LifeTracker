package com.dmitrysukhov.lifetracker.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class TopBarState(
    val title: String = "", val color: Color = PineColor,
    val topBarActions: @Composable() (RowScope.() -> Unit) = {}
)