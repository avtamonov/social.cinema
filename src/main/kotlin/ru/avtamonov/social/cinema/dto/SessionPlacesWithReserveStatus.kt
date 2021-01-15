package ru.avtamonov.social.cinema.dto

import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.model.Place

data class SessionPlacesWithReserveStatus(
    val freePlaces: List<Int>,
    val reservedPlaces: Map<Int, Place>,
    val status: Status,
    val income: Double
)
