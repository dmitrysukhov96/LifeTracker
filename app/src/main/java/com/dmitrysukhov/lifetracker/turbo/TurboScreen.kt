package com.dmitrysukhov.lifetracker.turbo

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import com.dmitrysukhov.lifetracker.FAB_EXPLODE_BOUNDS_KEY
import com.dmitrysukhov.lifetracker.utils.PineColor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.TurboScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Column(
        modifier = Modifier
            .background(PineColor)
            .fillMaxSize()
//            .sharedBounds(
//                sharedContentState = rememberSharedContentState(
//                    key = FAB_EXPLODE_BOUNDS_KEY
//                ),
//                animatedVisibilityScope = animatedVisibilityScope
//            )
    ) {
        Text("Main content")
    }
}

const val TURBO_SCREEN = "Turbo Screen"