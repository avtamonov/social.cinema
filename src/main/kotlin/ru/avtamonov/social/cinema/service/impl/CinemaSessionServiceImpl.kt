package ru.avtamonov.social.cinema.service.impl

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.avtamonov.social.cinema.util.SessionUtil
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
import javax.annotation.PostConstruct

/**
 * @param sessionOptions - конфигурация для сеансов
 * @param clock - системное время, позволяющее конфигурировать по часовым поясам
 * @param cinemaSessions - Репозиторий сеансов решил реализовывать на Map<UUID, CinemaSession> в виду ограниченного времени, было бы больше времени, подключил H2.
* */
@Service
class CinemaSessionServiceImpl (
    private val sessionOptions: SessionOptions,
    private val clock: Clock,
    private val cinemaSessions: MutableMap<UUID, CinemaSession>
) : CinemaSessionService {

    /**
     *  Флаг включенной скидки
     * */
    private var isDiscountOn = true

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse {
        val cinemaSession = CinemaSessionMapper.toModel(newSession, LocalDateTime.now(clock), sessionOptions.delayTimeForStandardCategory)
        cinemaSessions[cinemaSession.id] = cinemaSession
        return CinemaSessionMapper.toResponse(cinemaSession)
    }

    override fun getCinemaSessions(): List<CinemaSessionResponse> {
        return cinemaSessions.filter { it.value.startSessionDate.isAfter(LocalDateTime.now(clock)) }.map { CinemaSessionMapper.toResponse(it.value) }
    }

    override fun transferSessionTime(transferRequest: TransferRequest): Status {
        val session = cinemaSessions[transferRequest.idCinemaSession]
        return if (session != null) {
            cinemaSessions[session.id] = session.copy(
                startSessionDate = transferRequest.transferTime, // Переносим время начала сеанса
                startReserveForStandardCategory = transferRequest.transferTime.minusMinutes(sessionOptions.delayTimeForStandardCategory), // Переносим начало продаж станд. категории
                isStartReserveForStandardCategoryWasTransferred = false) // Меняем значение, чтобы шедулер заново расчитал, можно ли открывать продаже для станд. категории
            Status.OK
        } else {
            throw ResourceNotFoundException("Сеанс с id:${transferRequest.idCinemaSession} не найден.")
        }
    }

    override fun deleteCinemaSession(id: UUID) {
        cinemaSessions.remove(id)
    }

    override fun reservePlacesOnSession(reserveRequest: ReserveRequest, login: String, category: Int): CinemaSessionResponse {
        val session = cinemaSessions[reserveRequest.idCinemaSession]
        return if (session != null) {
            SessionUtil.validReserve(session, category, LocalDateTime.now(clock), isDiscountOn) // Валидируем запрос на бронирование
            val placesWithStatus = SessionUtil.reservePlaces(session, reserveRequest.places, login, category, sessionOptions, isDiscountOn) // Получаем расчёты по сеансу
            val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces, totalIncome = placesWithStatus.income) // обновляем сеанс и кладём в мапу
            cinemaSessions[session.id] = updatedSession
            CinemaSessionMapper.toResponse(updatedSession) // преобразуем ответ
        } else {
            throw ResourceNotFoundException("Сеанс с id:${reserveRequest.idCinemaSession} не найден.")
        }
    }

    override fun unReservePlacesOnSession(unReserveRequest: ReserveRequest, login: String): CinemaSessionResponse {
        val session = cinemaSessions[unReserveRequest.idCinemaSession]
        return if (session != null) {
            val placesWithStatus = SessionUtil.unReservePlaces(session, unReserveRequest.places, login) //Расчёты по сеансу
            if (placesWithStatus.status != Status.ERROR) {
                val updatedSession = session.copy(freePlaces = placesWithStatus.freePlaces, reservedPlaces = placesWithStatus.reservedPlaces, totalIncome = placesWithStatus.income) // обновляем сеанс и кладём в мапу
                cinemaSessions[session.id] = updatedSession
                CinemaSessionMapper.toResponse(updatedSession)
            } else {
                throw ValidationException("Вы пытались отменить чужую бронь или свободные места.")
            }
        } else {
            throw ResourceNotFoundException("Сеанс с id:${unReserveRequest.idCinemaSession} не найден.")
        }
    }

    override fun getSessionHistoryByLogin(login: String): List<SessionHistoryResponse> {
        val now = LocalDateTime.now(clock)
        return cinemaSessions
            .filterValues { it.reservedPlaces.filterValues { v -> v.login == login }.isNotEmpty() && it.startSessionDate.isBefore(now) }.values.toList() // фильтруем по логину и дате начала сеанса (чтобы она была не больше текущей) и приводим к списку
            .map { CinemaSessionMapper.toHistoryResponse(it) } // мапим ответ
    }

    @Scheduled(fixedRate = 5000)
    override fun checkTimeForStandardCategory() {
        val sessionsToUpdate = mutableMapOf<UUID, CinemaSession>() // мапа сеансов, которые нужно обновить в мапе всех сеансов
        val now = LocalDateTime.now(clock)
        cinemaSessions
            .filter { !it.value.isStartReserveForStandardCategoryWasTransferred } // отфильтровываем сеансы, по которым не началась продажа станд. категории
            .forEach {
                if (SessionUtil.needToOpenStandardCategoryReserve(it.value, sessionOptions, now)) { // проверка необходимости начала продаж для станд. категории
                    sessionsToUpdate[it.key] = it.value.copy(startReserveForStandardCategory = now, isStartReserveForStandardCategoryWasTransferred = true) // обновляем время начала продажи для стнд категории
                }
        }
        sessionsToUpdate.forEach {
            logger.info("Обновлено время начала продаж для несоциальной категории клиентов у сеанса id:${it.key}")
            cinemaSessions[it.key] = it.value // обновляем мапу со всеми сеансами
        }
    }

    override fun setDiscountMode(mode: Boolean): DiscountMode {
        isDiscountOn = mode
        return DiscountMode(isDiscountOn)
    }
}