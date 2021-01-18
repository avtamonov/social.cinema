package ru.avtamonov.social.cinema.model

import java.time.LocalDateTime
import java.util.*

/**
 * Сущность сеанса
 * @param id - id
 * @param countOfPlaces - количество мест на сеансе
 * @param filmName - название фильма
 * @param freePlaces - свободные места
 * @param reservedPlaces - забронированные места
 * @param startSessionDate - начало сеанса
 * @param totalIncome - прибыль за сеанс
 * @param standardPrice - цена билета
 * @param dateCreate - дата создания
 * @param startReserveForStandardCategory - начало бронирования для станд. категории
 * @param isStartReserveForStandardCategoryWasTransferred - признак открытия продаж для стандартной категории раньше времени
* */
data class CinemaSession(
    val id: UUID = UUID.randomUUID(),
    val countOfPlaces: Int,
    val filmName: String,
    val freePlaces: List<Int>,
    val reservedPlaces: Map<Int, Place> = mapOf(),
    val startSessionDate: LocalDateTime,
    val totalIncome: Double,
    val standardPrice: Double,
    val dateCreate: LocalDateTime,
    val startReserveForStandardCategory: LocalDateTime,
    val isStartReserveForStandardCategoryWasTransferred: Boolean = false
)

/**
 * Сущность места в зале
 * @param login - логин
 * @param price - цена
 * */
data class Place (
    val login: String,
    val price: Double
)