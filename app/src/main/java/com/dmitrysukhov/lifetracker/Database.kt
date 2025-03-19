package com.dmitrysukhov.lifetracker

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dmitrysukhov.lifetracker.utils.Category
import com.dmitrysukhov.lifetracker.utils.Event
import com.dmitrysukhov.lifetracker.utils.TodoItem

@Database(entities = [TodoItem::class, Category::class, Event::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun eventDao(): EventDao
}