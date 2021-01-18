package ru.avtamonov.social.cinema.dto

import java.time.LocalDateTime
import java.util.*

data class CinemaSessionResponse(
    val id: UUID,
    val countOfPlaces: Int = 0,
    val filmName: String = "",
    val freePlaces: List<Int> = listOf(),
    val reservedPlaces: List<ReservedPlaces> = listOf(),
    val startSessionDate: LocalDateTime,
    val dateCreate: LocalDateTime,
    val totalIncome: Double = 0.0,
    val startReserveForStandardCategory: LocalDateTime
)

data class ReservedPlaces (
    val place: Int,
    val login: String
)
