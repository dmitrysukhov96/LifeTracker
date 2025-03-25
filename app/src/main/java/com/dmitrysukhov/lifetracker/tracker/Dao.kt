package com.dmitrysukhov.lifetracker.tracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dmitrysukhov.lifetracker.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE (startTime BETWEEN :startMillis AND :endMillis OR endTime BETWEEN :startMillis AND :endMillis)")
    fun getEventsForPeriod(startMillis: Long, endMillis: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE projectId = :projectId AND endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getLastEventForProject(projectId: Long): Flow<Event?>
}