package com.dmitrysukhov.lifetracker.habits

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: HabitEvent)

    @Query("SELECT * FROM habit_events WHERE habitId = :habitId")
    fun getEventsForHabit(habitId: Long): Flow<List<HabitEvent>>

    @Query("DELETE FROM habit_events WHERE habitId = :habitId AND date = :date")
    suspend fun deleteEvent(habitId: Long, date: Long)

    @Query("DELETE FROM habit_events WHERE habitId = :habitId")
    suspend fun deleteAllEventsForHabit(habitId: Long)
}
