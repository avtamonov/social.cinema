package ru.avtamonov.social.cinema.service.impl

import org.springframework.stereotype.Service
import ru.avtamonov.social.cinema.dto.*
import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ResourceNotFoundException
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import ru.avtamonov.social.cinema.mapper.CinemaSessionMapper
import ru.avtamonov.social.cinema.model.CinemaSession
import ru.avtamonov.social.cinema.service.CinemaSessionService
import java.util.*

@Service
class CinemaSessionServiceImpl : CinemaSessionService {

    private val cinemaSessionList = mutableListOf<CinemaSession>()

    override fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse {
        val cinemaSession = CinemaSessionMapper.toModel(newSession)
        cinemaSessionList.add(cinemaSession)
        return CinemaSessionMapper.toResponse(cinemaSession)
    }

    override fun getCinemaSessions(): List<CinemaSessionResponse> {
        return cinemaSessionList.map { CinemaSessionMapper.toResponse(it) }
    }

    override fun transferSessionTime(transferRequest: TransferRequest): Status {
        val sessionIndex = cinemaSessionList.indexOfFirst { it.id == transferRequest.idCinemaSession }
        return if (sessionIndex != -1) {
            val session = cinemaSessionList.removeAt(sessionIndex)
            cinemaSessionList.add(session.copy(startSessionDate = transferRequest.transferTime))
            Status.OK
        } else {
            Status.ERROR
        }
    }

    override fun deleteCinemaSession(id: UUID) {
        val sessionIndex = cinemaSessionList.indexOfFirst { it.id == id }
        cinemaSessionList.removeAt(sessionIndex)
    }

    override fun reservePlacesOnSession(reserveRequest: ReserveRequest): CinemaSessionResponse {
        val sessionIndex = cinemaSessionList.indexOfFirst { it.id == reserveRequest.idCinemaSession }
        return if (sessionIndex != -1) {
            val session = cinemaSessionList.removeAt(sessionIndex)
            val placesWithStatus = reservePlaces(session.freePlaces, session.reservedPlaces, reserveRequest.places)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces)
                cinemaSessionList.add(sessionIndex, updatedSession)
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                cinemaSessionList.add(sessionIndex, session)
                throw ValidationException("Вы пытались забронировать уже занятые места")
            }

        } else {
            throw ResourceNotFoundException("Сеанс с id:${reserveRequest.idCinemaSession} не найден")
        }
    }

    override fun unReservePlacesOnSession(reserveRequest: ReserveRequest): CinemaSessionResponse {
        val sessionIndex = cinemaSessionList.indexOfFirst { it.id == reserveRequest.idCinemaSession }
        return if (sessionIndex != -1) {
            val session = cinemaSessionList.removeAt(sessionIndex)
            val placesWithStatus = unReservePlaces(session.freePlaces, session.reservedPlaces,  reserveRequest.places)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces)
                cinemaSessionList.add(sessionIndex, updatedSession)
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                cinemaSessionList.add(sessionIndex, session)
                throw ValidationException("Вы пытались забронировать уже занятые места")
            }

        } else {
            throw ResourceNotFoundException("Сеанс с id:${reserveRequest.idCinemaSession} не найден")
        }
    }

    private fun reservePlaces(freePlaces: List<Int>, reservedPlaces: List<Int>, placesToReserve: List<Int>): SessionPlacesWithReserveStatus {
        val newFreePlaces = freePlaces.toMutableList()
        val newReservedPlaces = reservedPlaces.toMutableList()
        var isReserveSuccess = true
        placesToReserve.forEach {
            if (newFreePlaces.contains(it)) {
                newReservedPlaces.add(it)
            } else {
                isReserveSuccess = false
            }
        }
        return if (isReserveSuccess) {
            newFreePlaces.removeIf { it in placesToReserve }
            newFreePlaces.sort()
            newReservedPlaces.sort()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK)
        } else {
            SessionPlacesWithReserveStatus(freePlaces, reservedPlaces, Status.ERROR)
        }
    }

    private fun unReservePlaces(freePlaces: List<Int>, reservedPlaces: List<Int>, placesToUnReserve: List<Int>): SessionPlacesWithReserveStatus {
        val newFreePlaces = freePlaces.toMutableList()
        val newReservedPlaces = reservedPlaces.toMutableList()
        var isUnReserveSuccess = true
        placesToUnReserve.forEach {
            if (newReservedPlaces.contains(it)) {
                newFreePlaces.add(it)
            } else {
                isUnReserveSuccess = false
            }
        }
        return if (isUnReserveSuccess) {
            newReservedPlaces.removeIf { it in placesToUnReserve }
            newFreePlaces.sort()
            newReservedPlaces.sort()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK)
        } else {
            SessionPlacesWithReserveStatus(freePlaces, reservedPlaces, Status.ERROR)
        }
    }
}