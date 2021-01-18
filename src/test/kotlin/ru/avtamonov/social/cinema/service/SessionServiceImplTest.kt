package ru.avtamonov.social.cinema.service

import io.mockk.spyk
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be`
import org.assertj.core.api.Assertions
import org.junit.Test
import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.ReserveRequest
import ru.avtamonov.social.cinema.dto.SessionOptions
import ru.avtamonov.social.cinema.dto.TransferRequest
import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ResourceNotFoundException
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import ru.avtamonov.social.cinema.model.CinemaSession
import ru.avtamonov.social.cinema.service.impl.CinemaSessionServiceImpl
import java.lang.Thread.sleep
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class SessionServiceImplTest {

    private val sessionOptions = SessionOptions()
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val cinemaSessions = mutableMapOf<UUID, CinemaSession>()
    private val sessionService = spyk(CinemaSessionServiceImpl(sessionOptions, clock, cinemaSessions))

    @Test
    fun `on createCinemaSession should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val result = sessionService.createCinemaSession(newSession)
        result.countOfPlaces `should be` 1
        result.reservedPlaces.size `should be` 0
        result.freePlaces.size `should be` 1
    }

    @Test
    fun `on getCinemaSessions should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        sessionService.createCinemaSession(newSession)
        val result = sessionService.getCinemaSessions()
        result.size `should be` 1
        val session = result.first()
        session.countOfPlaces `should be` 1
        session.reservedPlaces.size `should be` 0
        session.freePlaces.size `should be` 1
    }

    @Test
    fun `on transferSessionTime should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val createdSession = sessionService.createCinemaSession(newSession)
        val transferTime = createdSession.startSessionDate.plusMinutes(20)
        val status = sessionService.transferSessionTime(TransferRequest(createdSession.id, transferTime))
        status `should be` Status.OK
        val result = sessionService.getCinemaSessions()
        result.size `should be` 1
        val session = result.first()
        session.countOfPlaces `should be` 1
        session.reservedPlaces.size `should be` 0
        session.freePlaces.size `should be` 1
    }

    @Test
    fun `on transferSessionTime should throw exception`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val createdSession = sessionService.createCinemaSession(newSession)
        val transferTime = createdSession.startSessionDate.plusMinutes(20)
        Assertions.assertThatThrownBy {
            sessionService.transferSessionTime(TransferRequest(UUID.randomUUID(), transferTime))
        }.isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `on deleteCinemaSession should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        sessionService.deleteCinemaSession(session.id)
        val result = sessionService.getCinemaSessions()
        result.size `should be` 0
    }

    @Test
    fun `on reservePlacesOnSession should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        val result = sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 1)
        result.reservedPlaces.size `should be` 1
        result.freePlaces.size `should be` 0
    }

    @Test
    fun `on reservePlacesOnSession should be ok when category is standard`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        sleep(10000)
        val result = sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 0)
        result.reservedPlaces.size `should be` 1
        result.freePlaces.size `should be` 0
    }

    @Test
    fun `on reservePlacesOnSession should throw exception`() {
        Assertions.assertThatThrownBy {
            sessionService.reservePlacesOnSession(ReserveRequest(UUID.randomUUID(), places = listOf(1)), "test", 1)
        }.isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `on reservePlacesOnSession should throw exception when choose reserved places`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 1)
        Assertions.assertThatThrownBy {
            sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 1)
        }.isInstanceOf(ValidationException::class.java)
    }

    @Test
    fun `on reservePlacesOnSession should throw exception when choose standard category before sale is open`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock).plusMinutes(30))
        val session = sessionService.createCinemaSession(newSession)
        Assertions.assertThatThrownBy {
            sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 0)
        }.isInstanceOf(ValidationException::class.java)
    }

    @Test
    fun `on unReservePlacesOnSession should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 1)
        val result = sessionService.unReservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test")
        result.reservedPlaces.size `should be` 0
        result.freePlaces.size `should be` 1
    }

    @Test
    fun `on unReservePlacesOnSession should throw exception`() {
        Assertions.assertThatThrownBy {
            sessionService.unReservePlacesOnSession(ReserveRequest(UUID.randomUUID(), places = listOf(1)), "test")
        }.isInstanceOf(ResourceNotFoundException::class.java)
    }

    @Test
    fun `on getSessionHistoryByLogin should be ok`() {
        val newSession = CinemaSessionCreateDto(countOfPlaces = 1, startSessionDate = LocalDateTime.now(clock))
        val session = sessionService.createCinemaSession(newSession)
        sessionService.reservePlacesOnSession(ReserveRequest(session.id, places = listOf(1)), "test", 1)
        sessionService.transferSessionTime(TransferRequest(session.id, session.startSessionDate.minusMinutes(20)))
        val result = sessionService.getSessionHistoryByLogin("test")
        result.size `should be` 1
    }
}
