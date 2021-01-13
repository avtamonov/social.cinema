package ru.avtamonov.social.cinema.mapper

import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.CinemaSessionResponse
import ru.avtamonov.social.cinema.model.CinemaSession

class CinemaSessionMapper {
    companion object {
        fun toModel(cinemaSessionDto: CinemaSessionCreateDto): CinemaSession {
            return CinemaSession(
                countOfPlaces = cinemaSessionDto.countOfPlaces,
                filmName = cinemaSessionDto.filmName,
                freePlaces = (1..cinemaSessionDto.countOfPlaces).toList(),
                reservedPlaces = listOf(),
                startSessionDate = cinemaSessionDto.startSessionDate
            )
        }

        fun toResponse(cinemaSession: CinemaSession): CinemaSessionResponse {
            return CinemaSessionResponse(
                id = cinemaSession.id,
                countOfPlaces = cinemaSession.countOfPlaces,
                filmName = cinemaSession.filmName,
                freePlaces = cinemaSession.freePlaces,
                reservedPlaces = cinemaSession.reservedPlaces,
                startSessionDate = cinemaSession.startSessionDate
            )
        }
    }
}