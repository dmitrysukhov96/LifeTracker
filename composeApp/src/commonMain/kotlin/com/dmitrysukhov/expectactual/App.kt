package com.dmitrysukhov.expectactual

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import expectactual.composeapp.generated.resources.Res
import expectactual.composeapp.generated.resources.compose_multiplatform
import expectactual.composeapp.generated.resources.interop_dt
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    batteryManager: BatteryManager
) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("The current battery level is ${batteryManager.getBatteryLevel()}")
            Image(painterResource(Res.drawable.interop_dt), null)
        }
    }
}