package com.dmitrysukhov.expectactual

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExpectActual",
    ) {
        App(remember { BatteryManager() })
    }
}