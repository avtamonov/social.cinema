package ru.avtamonov.social.cinema.service.impl

import org.slf4j.LoggerFactory
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

    private val cinemaSessions = mutableMapOf<UUID, CinemaSession>()

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse {
        val cinemaSession = CinemaSessionMapper.toModel(newSession)
        cinemaSessions[cinemaSession.id] = cinemaSession
        return CinemaSessionMapper.toResponse(cinemaSession)
    }

    override fun getCinemaSessions(): List<CinemaSessionResponse> {
        return cinemaSessions.map { CinemaSessionMapper.toResponse(it.value) }
    }

    override fun transferSessionTime(transferRequest: TransferRequest): Status {
        val session = cinemaSessions[transferRequest.idCinemaSession]
        return if (session != null) {
            cinemaSessions[session.id] = session.copy(startSessionDate = transferRequest.transferTime)
            Status.OK
        } else {
            Status.ERROR
        }
    }

    override fun deleteCinemaSession(id: UUID) {
        cinemaSessions.remove(id)
    }

    override fun reservePlacesOnSession(reserveRequest: ReserveRequest, login: String, category: Int): CinemaSessionResponse {
        val session = cinemaSessions[reserveRequest.idCinemaSession]
        return if (session != null) {
            val placesWithStatus = reservePlaces(session.freePlaces, session.reservedPlaces, reserveRequest.places, login)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces)
                cinemaSessions[session.id] = updatedSession
                logger.info("Успешная бронь")
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                logger.error("Вы пытались забронировать уже занятые места")
                throw ValidationException("Вы пытались забронировать уже занятые места")
            }

        } else {
            throw ResourceNotFoundException("Сеанс с id:${reserveRequest.idCinemaSession} не найден")
        }
    }

    override fun unReservePlacesOnSession(unReserveRequest: ReserveRequest, login: String): CinemaSessionResponse {
        val session = cinemaSessions[unReserveRequest.idCinemaSession]
        return if (session != null) {
            val placesWithStatus = unReservePlaces(session.freePlaces, session.reservedPlaces,  unReserveRequest.places, login)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces)
                cinemaSessions[session.id] = updatedSession
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                throw ValidationException("Вы пытались отменить чужую бронь или свободные места")
            }

        } else {
            throw ResourceNotFoundException("Сеанс с id:${unReserveRequest.idCinemaSession} не найден")
        }
    }

    private fun reservePlaces(freePlaces: List<Int>, reservedPlaces: Map<Int, String>, placesToReserve: List<Int>, login: String): SessionPlacesWithReserveStatus {
        val newFreePlaces = freePlaces.toMutableList()
        val newReservedPlaces = reservedPlaces.toMutableMap()
        var isReserveSuccess = true
        placesToReserve.forEach {
            if (newFreePlaces.contains(it)) {
                newReservedPlaces[it] = login
            } else {
                isReserveSuccess = false
            }
        }
        return if (isReserveSuccess) {
            newFreePlaces.removeIf { it in placesToReserve }
            newFreePlaces.sort()
            newReservedPlaces.toSortedMap()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK)
        } else {
            SessionPlacesWithReserveStatus(freePlaces, reservedPlaces, Status.ERROR)
        }
    }

    private fun unReservePlaces(freePlaces: List<Int>, reservedPlaces: Map<Int, String>, placesToUnReserve: List<Int>, login: String): SessionPlacesWithReserveStatus {
        val newFreePlaces = freePlaces.toMutableList()
        val newReservedPlaces = reservedPlaces.toMutableMap()
        var isUnReserveSuccess = true
        placesToUnReserve.forEach {
            val reservedPlaceLogin = newReservedPlaces[it]
            if (reservedPlaceLogin != null && reservedPlaceLogin == login) {
                newFreePlaces.add(it)
                newReservedPlaces.remove(it)
            } else {
                isUnReserveSuccess = false
            }
        }
        return if (isUnReserveSuccess) {
            newFreePlaces.sort()
            newReservedPlaces.toSortedMap()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK)
        } else {
            SessionPlacesWithReserveStatus(freePlaces, reservedPlaces, Status.ERROR)
        }
    }

}