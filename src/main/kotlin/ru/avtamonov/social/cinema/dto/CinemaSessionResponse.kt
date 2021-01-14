package ru.avtamonov.social.cinema.dto

import java.time.LocalDateTime
import java.util.*

data class CinemaSessionResponse(
    val id: UUID,
    val countOfPlaces: Int,
    val filmName: String,
    val freePlaces: List<Int>,
    val reservedPlaces: List<Int>,
    val startSessionDate: LocalDateTime
)