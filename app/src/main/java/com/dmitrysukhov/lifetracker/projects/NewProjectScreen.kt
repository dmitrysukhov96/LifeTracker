package com.dmitrysukhov.lifetracker.projects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.dmitrysukhov.lifetracker.utils.TopBarState


@Composable
fun NewProjectScreen(setTopBarState: (TopBarState) -> Unit) {
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(""))
    }
}

const val NEW_PROJECT_SCREEN = "new_project_screen"