package ru.avtamonov.social.cinema.mapper

import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.CinemaSessionResponse
import ru.avtamonov.social.cinema.dto.ReservedPlaces
import ru.avtamonov.social.cinema.dto.SessionHistoryResponse
import ru.avtamonov.social.cinema.model.CinemaSession

class CinemaSessionMapper {
    companion object {
        fun toModel(cinemaSessionDto: CinemaSessionCreateDto): CinemaSession {
            return CinemaSession(
                countOfPlaces = cinemaSessionDto.countOfPlaces,
                filmName = cinemaSessionDto.filmName,
                freePlaces = (1..cinemaSessionDto.countOfPlaces).toList(),
                reservedPlaces = mapOf(),
                startSessionDate = cinemaSessionDto.startSessionDate,
                totalIncome = 0.0,
                standardPrice = cinemaSessionDto.standardPrice
            )
        }

        fun toResponse(cinemaSession: CinemaSession): CinemaSessionResponse {
            return CinemaSessionResponse(
                id = cinemaSession.id,
                countOfPlaces = cinemaSession.countOfPlaces,
                filmName = cinemaSession.filmName,
                freePlaces = cinemaSession.freePlaces,
                reservedPlaces = cinemaSession.reservedPlaces.map { ReservedPlaces(it.key, it.value.login) },
                startSessionDate = cinemaSession.startSessionDate,
                totalIncome = cinemaSession.totalIncome
            )
        }

        fun toHistoryResponse(cinemaSession: CinemaSession): SessionHistoryResponse {
            return SessionHistoryResponse(
                id = cinemaSession.id,
                filmName = cinemaSession.filmName,
                startSessionDate = cinemaSession.startSessionDate
            )
        }
    }
}