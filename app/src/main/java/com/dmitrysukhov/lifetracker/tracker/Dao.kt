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
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEvent(eventId: Long)

    @Query("SELECT * FROM events WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime ASC")
    fun getEventsForPeriod(startTime: Long, endTime: Long): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY startTime DESC LIMIT 1")
    fun getLastEvent(): Flow<Event?>
}