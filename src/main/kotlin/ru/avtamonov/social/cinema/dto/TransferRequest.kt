package ru.avtamonov.social.cinema.dto

import java.time.LocalDateTime
import java.util.*

data class TransferRequest (
    val idCinemaSession: UUID,
    val transferTime: LocalDateTime
)
