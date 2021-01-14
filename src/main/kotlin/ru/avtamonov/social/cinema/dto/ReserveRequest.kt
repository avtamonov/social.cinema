package ru.avtamonov.social.cinema.dto

import java.util.*

data class ReserveRequest (
    val idCinemaSession: UUID,
    val places: List<Int>
)
