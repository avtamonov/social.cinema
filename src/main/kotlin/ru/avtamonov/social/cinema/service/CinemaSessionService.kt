package ru.avtamonov.social.cinema.service

import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.CinemaSessionResponse
import ru.avtamonov.social.cinema.dto.ReserveRequest
import ru.avtamonov.social.cinema.dto.TransferRequest
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

    fun reservePlacesOnSession(reserveRequest: ReserveRequest): CinemaSessionResponse

    fun unReservePlacesOnSession(reserveRequest: ReserveRequest): CinemaSessionResponse
}