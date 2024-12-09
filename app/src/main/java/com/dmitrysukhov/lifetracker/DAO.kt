package com.dmitrysukhov.lifetracker.android.data

import androidx.room.*
import com.dmitrysukhov.lifetracker.TodoItem

@Dao
interface TodoDao {
    @Query("SELECT * FROM TodoItem")
    suspend fun getAllTasks(): List<TodoItem>

    @Insert
    suspend fun insertTask(task: TodoItem)

    @Update
    suspend fun updateTask(task: TodoItem)

    @Delete
    suspend fun deleteTask(task: TodoItem)
}
