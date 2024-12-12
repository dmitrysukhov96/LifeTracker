package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun TrackerScreen(navController: NavHostController) {
    Column(Modifier.fillMaxSize().background(BgColor)) {  }
}
const val TRACKER_SCREEN = "Tracker"