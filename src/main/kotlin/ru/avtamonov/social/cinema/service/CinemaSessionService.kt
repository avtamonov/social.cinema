package ru.avtamonov.social.cinema.service

import ru.avtamonov.social.cinema.dto.*
import ru.avtamonov.social.cinema.enum.Status
import java.util.*

/**
 * Сервис создания, редактирования, чтения сеансов
 * */
interface CinemaSessionService {
    fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse

    fun getCinemaSessions(): List<CinemaSessionResponse>

    fun transferSessionTime(transferRequest: TransferRequest): Status

    fun deleteCinemaSession(id: UUID)

    fun reservePlacesOnSession(reserveRequest: ReserveRequest, login: String, category: Int): CinemaSessionResponse

    fun unReservePlacesOnSession(unReserveRequest: ReserveRequest, login: String): CinemaSessionResponse

    fun getSessionHistoryByLogin(login: String): List<SessionHistoryResponse>
}