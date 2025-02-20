package com.dmitrysukhov.lifetracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dmitrysukhov.lifetracker.utils.Category
import com.dmitrysukhov.lifetracker.utils.Event
import com.dmitrysukhov.lifetracker.utils.TodoItem

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo")
    suspend fun getAllTasks(): List<TodoItem>

    @Insert
    suspend fun insertTask(task: TodoItem)

    @Update
    suspend fun updateTask(task: TodoItem)

    @Delete
    suspend fun deleteTask(task: TodoItem)
}


@Dao
interface EventDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Insert
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event

//    @Query("SELECT * FROM categories")
//    suspend fun getAllCategories(): List<Category>
}