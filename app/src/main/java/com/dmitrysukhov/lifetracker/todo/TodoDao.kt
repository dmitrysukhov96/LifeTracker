package com.dmitrysukhov.lifetracker.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dmitrysukhov.lifetracker.TodoItem

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