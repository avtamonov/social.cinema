package ru.avtamonov.social.cinema.util

import ru.avtamonov.social.cinema.dto.SessionOptions
import ru.avtamonov.social.cinema.dto.SessionPlacesWithReserveStatus
import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import ru.avtamonov.social.cinema.model.CinemaSession
import ru.avtamonov.social.cinema.model.Place
import java.time.LocalDateTime

class SessionUtil {
    companion object {
        /**
         * Метод расчёта цены в зависимости от соц.категории клиента
         * @param category - социальная категория
         * @param price - стандартная цена за билет
         * @param sessionOptions - конфигурация для сеансов
         * @throws ValidationException если категория в запросе не в диапазоне [0;3]
         * @return цена билета
        * */
        private fun calculateIncome(category: Int, price: Double, sessionOptions: SessionOptions): Double {
            return when(category) {
                0 -> price
                1 -> price * (100 - sessionOptions.discount1) / 100
                2 -> price * (100 - sessionOptions.discount2) / 100
                3 -> price * (100 - sessionOptions.discount3) / 100
                else -> throw ValidationException("Не существует скидки для категории $category.")
            }
        }

        /**
         * Валидация при бронировании, которая запрещает бронировать
         * стандартной категории до начала продаж для стандартной категории
         * */
        fun validReserve(session: CinemaSession, category: Int, now: LocalDateTime) {
            if (category == 0 && now.isBefore(session.startReserveForStandardCategory)) {
                throw ValidationException("К сожалению, зарезервировать билеты для не социальной категории сейчас невозможно.")
            }
        }

        /**
         * Метод для бронирования билетов на сеанс. Идет перерасчёт прибыли за сеанс.
         * @param session - сеанс
         * @param placesToReserve - места для бронирования
         * @param login - логин клиента (для сверки с логином, который забронировал эти места)
         * @param category - социальная категория
         * @param sessionOptions - конфигурация для сеансов
         * @return SessionPlacesWithReserveStatus - дто, в котором лежит статус бронирования,
         * свободные места на сеансе, занятые места на сеансе, расчёт прибыли
         * */
        fun reservePlaces(
            session: CinemaSession,
            placesToReserve: List<Int>,
            login: String,
            category: Int,
            sessionOptions: SessionOptions
        ): SessionPlacesWithReserveStatus {
            val newFreePlaces = session.freePlaces.toMutableList()
            val newReservedPlaces = session.reservedPlaces.toMutableMap()
            var totalIncome = session.totalIncome
            placesToReserve.forEach {
                if (newFreePlaces.contains(it)) {
                    val income = calculateIncome(category, session.standardPrice, sessionOptions)
                    newReservedPlaces[it] = Place(login, income)
                    totalIncome += income
                } else {
                    throw ValidationException("Вы пытались забронировать уже занятые места.")
                }
            }
            newFreePlaces.removeIf { it in placesToReserve }
            newFreePlaces.sort()
            newReservedPlaces.toSortedMap()
            return SessionPlacesWithReserveStatus(newFreePlaces, newReservedPlaces, Status.OK, totalIncome)
        }

        /**
         * Метод для отмены бронирования билетов на сеанс. Идет перерасчёт прибыли за сеанс.
         * @param session - сеанс
         * @param placesToUnReserve - места для отмены бронирования
         * @param login - логин клиента (для сверки с логином, который забронировал эти места)
         * @return SessionPlacesWithReserveStatus - дто, в котором лежит статус отмены бронирования,
         * свободные места на сеансе, занятые места на сеансе, расчёт прибыли
         * */
        fun unReservePlaces(
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

        /**
         * Метод, который проверяет нужно ли открывать продажу для стандартной категории клиентов
         * @param session - сеанс
         * @param sessionOptions - конфигурация для сеансов
         * @param now - текущая дата
         * */
        fun needToOpenStandardCategoryReserve(session: CinemaSession, sessionOptions: SessionOptions, now: LocalDateTime): Boolean {
            val fullProfit = session.countOfPlaces.times(session.standardPrice)
            val sessionProfit = session.reservedPlaces.values.sumByDouble { it.price }
            val isProfitLessThenMinProfit = sessionProfit.div(fullProfit).times(100) < sessionOptions.minProfit // расчёт отношения текущей прибыли за сеанс к полной прибыли и сравнение с желаемым доходом
            return when {
                !isProfitLessThenMinProfit -> true // открытие продаж для стандартной категории если прибыль от соц. категорий привысила 70% без учета времени начала продаже станд. категории
                now.isEqual(session.dateCreate.plusMinutes(sessionOptions.paybackTime)) || now.isAfter(session.dateCreate.plusMinutes(sessionOptions.paybackTime)) -> isProfitLessThenMinProfit // открытие продаж для стандартной категории если за paybackTime было куплено мало билетов
                else -> false
            }
        }
    }

}