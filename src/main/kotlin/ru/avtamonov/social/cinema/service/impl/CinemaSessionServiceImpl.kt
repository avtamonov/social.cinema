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

    override fun reserveTicketOnSession(reserveRequest: ReserveRequest): CinemaSessionResponse {
        val sessionIndex = cinemaSessionList.indexOfFirst { it.id == reserveRequest.idCinemaSession }
        return if (sessionIndex != -1) {
            val sessionWithStatus = reserveSeats(cinemaSessionList.removeAt(sessionIndex), reserveRequest.placesToReserve)
            if (sessionWithStatus.status != Status.ERROR) {
                cinemaSessionList.add(sessionIndex, sessionWithStatus.session)
                CinemaSessionMapper.toResponse(sessionWithStatus.session)
            } else {
                cinemaSessionList.add(sessionIndex, sessionWithStatus.session)
                throw ValidationException("Вы пытались забронировать уже занятые места")
            }

        } else {
            throw ResourceNotFoundException("Сеанс с id:${reserveRequest.idCinemaSession} не найден")
        }
    }

    private fun reserveSeats(session: CinemaSession, seatsToReserve: List<Int>): SessionWithReserveStatus {
        val freePlaces = session.freePlaces.toMutableList()
        val reservedPlaces = session.reservedPlaces.toMutableList()
        var isReserveSuccess = true
        seatsToReserve.forEach {
            if (freePlaces.contains(it)) {
                reservedPlaces.add(it)
            } else {
                isReserveSuccess = false
            }
        }
        return if (isReserveSuccess) {
            freePlaces.removeIf { it in seatsToReserve }
            SessionWithReserveStatus(session.copy(freePlaces = freePlaces, reservedPlaces = reservedPlaces), Status.OK)
        } else {
            SessionWithReserveStatus(session, Status.ERROR)
        }
    }
}