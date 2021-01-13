package ru.avtamonov.social.cinema.dto

import java.time.LocalDateTime

data class CinemaSessionCreateDto (
    val filmName: String = "test",
    val countOfPlaces: Int,
    val startSessionDate: LocalDateTime
)
