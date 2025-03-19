package com.dmitrysukhov.lifetracker.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

data class TopBarState(
    val title: String = "",
    val topBarActions: @Composable() (RowScope.() -> Unit) = {},
    val rightIcon: @Composable () -> Unit,
    val leftIcon: @Composable () -> Unit
)