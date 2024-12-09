package com.dmitrysukhov.lifetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TodoItem")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isDone: Boolean
)
