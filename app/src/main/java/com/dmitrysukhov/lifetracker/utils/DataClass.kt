package com.dmitrysukhov.lifetracker.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isDone: Boolean
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val categoryId: Long,
    val name: String,
    val duration: Long? = null
)
