package ru.avtamonov.social.cinema.dto

import ru.avtamonov.social.cinema.enum.Status

data class SessionPlacesWithReserveStatus(
    val freePlaces: List<Int>,
    val reservedPlaces: Map<Int, String>,
    val status: Status
)
