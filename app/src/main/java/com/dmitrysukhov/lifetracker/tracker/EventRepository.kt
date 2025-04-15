package com.dmitrysukhov.lifetracker.tracker

import com.dmitrysukhov.lifetracker.Event
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {
    fun getEventsForTimeRange(startTime: Long, endTime: Long): Flow<List<Event>> {
        return eventDao.getEventsForPeriod(startTime, endTime)
    }

    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    fun getLastEvent(): Flow<Event?> {
        return eventDao.getLastEvent()
    }
}
