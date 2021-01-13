package ru.avtamonov.social.cinema.model

import java.time.LocalDateTime
import java.util.*

data class CinemaSession(
    val id: UUID = UUID.randomUUID(),
    val countOfPlaces: Int,
    val filmName: String,
    val freePlaces: List<Int>,
    val reservedPlaces: List<Int>,
    val startSessionDate: LocalDateTime
)
