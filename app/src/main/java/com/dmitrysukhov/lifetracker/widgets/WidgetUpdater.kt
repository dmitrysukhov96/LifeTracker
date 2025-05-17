package com.dmitrysukhov.lifetracker.widgets

import android.content.Context

object WidgetUpdater {
    fun updateWidgets(context: Context) {
        // Update the widgets when tracking status changes
        CurrentTaskWidgetProvider.updateAllWidgets(context)
    }
} 