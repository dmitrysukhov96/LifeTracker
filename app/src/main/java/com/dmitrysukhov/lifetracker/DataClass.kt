package com.dmitrysukhov.lifetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val description: String? = null,
    val projectId: Long? = null,
    val dateTime: Long? = null,
    val reminderTime: Long? = null,
    val repeatInterval: String? = null,
    val durationMinutes: Int? = null,
    val isDone: Boolean = false
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val projectId: Long = 0L,
    val title: String,
    val description: String = "",
    val color: Int,
    val photoUri: String? = null,
    val goal: String? = null,
    val deadlineMillis: Long? = null,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val projectId: Long,
    val name: String? = null,
    val startTime: Long,
    val endTime: Long?
)