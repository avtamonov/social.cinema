package ru.avtamonov.social.cinema.service

import ru.avtamonov.social.cinema.dto.*
import ru.avtamonov.social.cinema.enum.Status
import java.util.*

/**
 * Сервис создания, редактирования, чтения сеансов
 * */
interface CinemaSessionService {
    /**
     * Создать сеанс с необходимыми расчетами
     * @see CinemaSessionResponse
     * */
    fun createCinemaSession(newSession: CinemaSessionCreateDto): CinemaSessionResponse

    /**
     * Прочитать все сеансы
     * @see CinemaSessionResponse
     * */
    fun getCinemaSessions(): List<CinemaSessionResponse>

    /**
     * Перенести сеанс.
     * @param transferRequest дто с id сеанса и временем переноса
     * @return статус переноса сеанса
     * */
    fun transferSessionTime(transferRequest: TransferRequest): Status

    /**
     * Удалить сеанс.
     * @param id сеанса и временем переноса
     * */
    fun deleteCinemaSession(id: UUID)

    /**
     * Зарезервировать билеты на сеансе
     * @param reserveRequest - дто с id сеанса и местами для бронирования
     * @param login - логин клиента
     * @param category - категория клиента
     * @see CinemaSessionResponse
     * */
    fun reservePlacesOnSession(reserveRequest: ReserveRequest, login: String, category: Int): CinemaSessionResponse

    /**
     * Отменить бронирование билетов на сеансе
     * @param reserveRequest - дто с id сеанса и местами для отмены бронирования
     * @param login - логин клиента
     * @see CinemaSessionResponse
     * */
    fun unReservePlacesOnSession(unReserveRequest: ReserveRequest, login: String): CinemaSessionResponse

    /**
    * Прочитать историю посещённых сеансов
    * */
    fun getSessionHistoryByLogin(login: String): List<SessionHistoryResponse>

    /**
     * Метод для проверки открытия продаж стандартной категории клиента
     * */
    fun checkTimeForStandardCategory()

    /**
     * Метод включения и выключения скидок
     * */
    fun setDiscountMode(mode: Boolean): DiscountMode
}