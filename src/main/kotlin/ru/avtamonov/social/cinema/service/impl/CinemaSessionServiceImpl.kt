package ru.avtamonov.social.cinema.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.avtamonov.social.cinema.dto.*
import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ResourceNotFoundException
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import ru.avtamonov.social.cinema.mapper.CinemaSessionMapper
import ru.avtamonov.social.cinema.model.CinemaSession
import ru.avtamonov.social.cinema.model.Place
import ru.avtamonov.social.cinema.service.CinemaSessionService
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@Service
class CinemaSessionServiceImpl (
    private val sessionOptions: SessionOptions,
    private val clock: Clock
) : CinemaSessionService {

    private val cinemaSessions = mutableMapOf<UUID, CinemaSession>()

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse {
        val cinemaSession = CinemaSessionMapper.toModel(newSession, LocalDateTime.now(clock), sessionOptions.delayTimeForStandardCategory)
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
            val placesWithStatus = reservePlaces(session, reserveRequest.places, login, category)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces, totalIncome = placesWithStatus.income)
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
            val placesWithStatus = unReservePlaces(session, unReserveRequest.places, login)
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces, totalIncome = placesWithStatus.income)
                cinemaSessions[session.id] = updatedSession
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                throw ValidationException("Вы пытались отменить чужую бронь или свободные места")
            }
        } else {
            throw ResourceNotFoundException("Сеанс с id:${unReserveRequest.idCinemaSession} не найден")
        }
    }

    //FIXME Проблемы с оптимизацией
    override fun getSessionHistoryByLogin(login: String): List<SessionHistoryResponse> {
        return cinemaSessions
            .filterValues { it.reservedPlaces.filterValues { v -> v.login == login }.isNotEmpty() }.values.toList()
            .map { CinemaSessionMapper.toHistoryResponse(it) }
    }

    private fun reservePlaces(
        session: CinemaSession,
        placesToReserve: List<Int>,
        login: String,
        category: Int
    ): SessionPlacesWithReserveStatus {
        val newFreePlaces = session.freePlaces.toMutableList()
        val newReservedPlaces = session.reservedPlaces.toMutableMap()
        var isReserveSuccess = true
        var totalIncome = session.totalIncome
        placesToReserve.forEach {
            if (newFreePlaces.contains(it)) {
                val income = calculateIncome(category, session.standardPrice)
                newReservedPlaces[it] = Place(login, income)
                totalIncome += income
            } else {
                isReserveSuccess = false
            }
        }
        return if (isReserveSuccess) {
            newFreePlaces.removeIf { it in placesToReserve }
            newFreePlaces.sort()
            newReservedPlaces.toSortedMap()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK, totalIncome)
        } else {
            SessionPlacesWithReserveStatus(session.freePlaces, session.reservedPlaces, Status.ERROR, session.totalIncome)
        }
    }

    private fun unReservePlaces(
        session: CinemaSession,
        placesToUnReserve: List<Int>,
        login: String
    ): SessionPlacesWithReserveStatus {
        val newFreePlaces = session.freePlaces.toMutableList()
        val newReservedPlaces = session.reservedPlaces.toMutableMap()
        var isUnReserveSuccess = true
        var totalIncome = session.totalIncome
        placesToUnReserve.forEach {
            val reservedPlaceInfo = newReservedPlaces[it]
            if (reservedPlaceInfo != null && reservedPlaceInfo.login == login) {
                newFreePlaces.add(it)
                newReservedPlaces.remove(it)
                totalIncome -= reservedPlaceInfo.price
            } else {
                isUnReserveSuccess = false
            }
        }
        return if (isUnReserveSuccess) {
            newFreePlaces.sort()
            newReservedPlaces.toSortedMap()
            SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK, totalIncome)
        } else {
            SessionPlacesWithReserveStatus(session.freePlaces, session.reservedPlaces, Status.ERROR, session.totalIncome)
        }
    }

    private fun calculateIncome(category: Int, price: Double): Double {
        return when(category) {
            0 -> price
            1 -> price * (100 - sessionOptions.discount1) / 100
            2 -> price * (100 - sessionOptions.discount2) / 100
            3 -> price * (100 - sessionOptions.discount3) / 100
            else -> throw ValidationException("Не существует скидки для категории $category")
        }
    }

}