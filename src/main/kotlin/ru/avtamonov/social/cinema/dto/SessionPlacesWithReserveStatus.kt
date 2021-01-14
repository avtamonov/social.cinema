package ru.avtamonov.social.cinema.dto

import ru.avtamonov.social.cinema.enum.Status

data class SessionPlacesWithReserveStatus(
    val freePlaces: List<Int>,
    val reservedPlaces: List<Int>,
    val status: Status
)
