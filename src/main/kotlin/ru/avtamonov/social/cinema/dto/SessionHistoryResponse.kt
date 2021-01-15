package ru.avtamonov.social.cinema.dto

import java.time.LocalDateTime
import java.util.*

data class SessionHistoryResponse(
    val id: UUID,
    val filmName: String,
    val startSessionDate: LocalDateTime
)
