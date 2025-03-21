package com.dmitrysukhov.lifetracker.projects

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dmitrysukhov.lifetracker.utils.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<Project>>

    @Insert
    suspend fun insert(project: Project)
}