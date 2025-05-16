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
    val repeatInterval: String? = null,
    val estimatedDurationMs: Long? = null,
    val isDone: Boolean = false,
    val completeDate: Long? = null
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val projectId: Long = 0L,
    val title: String,
    val description: String = "",
    val color: Int,
    val photoUri: String? = null,
    val imagePath: String? = null,
    val goal: String? = null,
    val deadlineMillis: Long? = null,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0, val projectId: Long? = null,
    val name: String? = null, val startTime: Long, val endTime: Long?
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val type: Int,
    val color: Int
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val projectId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)