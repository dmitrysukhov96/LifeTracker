package com.dmitrysukhov.lifetracker.tracker

import javax.inject.Inject

class EventRepository @Inject constructor(private val eventDao: EventDao) {
//
//    // Вставка нового события в базу данных
//    suspend fun insertEvent(event: Event) {
//        eventDao.insertEvent(event)
//    }
//
//    // Обновление существующего события
//    suspend fun updateEvent(event: Event) {
//        eventDao.updateEvent(event)
//    }
//
//    // Получение последнего события для проекта (используем Flow для подписки на обновления)
//    fun getLastEventForProject(projectId: Long): Flow<Event?> {
//        return eventDao.getLastEventForProject(projectId)
//    }
//
//    // Получение всех событий для проекта в указанном периоде времени (также возвращаем Flow)
//    fun getEventsForPeriod(startMillis: Long, endMillis: Long): Flow<List<Event>> {
//        return eventDao.getEventsForPeriod(startMillis, endMillis)
//    }
}
