package ru.avtamonov.social.cinema.dto

import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.model.CinemaSession

data class SessionWithReserveStatus(
    val session: CinemaSession,
    val status: Status
)
