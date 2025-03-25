package com.dmitrysukhov.lifetracker

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import com.dmitrysukhov.lifetracker.todo.TodoDao
import com.dmitrysukhov.lifetracker.tracker.EventDao

@Database(entities = [TodoItem::class, Project::class, Event::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun projectsDao(): ProjectDao
    abstract fun eventDao(): EventDao
}