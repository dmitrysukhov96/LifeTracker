package com.dmitrysukhov.lifetracker.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun TrackerScreen(setTopBarState: (TopBarState) -> Unit) {
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("Tracker"))
    }
    Column(Modifier.fillMaxSize().background(BgColor)) {  }
}
const val TRACKER_SCREEN = "Tracker"