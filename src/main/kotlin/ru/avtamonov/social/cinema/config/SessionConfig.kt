package ru.avtamonov.social.cinema.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.SessionOptions
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import ru.avtamonov.social.cinema.mapper.CinemaSessionMapper
import ru.avtamonov.social.cinema.model.CinemaSession
import ru.avtamonov.social.cinema.util.SessionUtil
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

/**
 * Конфигурация параметров сеанса
 * */
@Configuration
class SessionConfig (
    private val clock: Clock
) {

    @Value("\${social-discount-1}")
    private var discount1 = 0

    @Value("\${social-discount-2}")
    private var discount2 = 0

    @Value("\${social-discount-3}")
    private var discount3 = 0

    @Value("\${delay-time-for-standard-category}")
    private var delayTimeForStandardCategory = 0L

    @Value("\${min-profit}")
    private var minProfit = 0

    @Value("\${payback-time}")
    private var paybackTime = 0L

    /**
     * Бин с настройками сеанса
     * */
    @Bean
    fun sessionOptions(): SessionOptions {
        validateDiscounts()
        return SessionOptions(
            discount1 = discount1,
            discount2 = discount2,
            discount3 = discount3,
            delayTimeForStandardCategory = delayTimeForStandardCategory,
            minProfit = minProfit,
            paybackTime = paybackTime
        )
    }

    /**
     * Бин с тестовыми данными
     * */
    @Bean
    fun cinemaSessions(): MutableMap<UUID, CinemaSession> {
        val now = LocalDateTime.now(clock)
        val delayTime = 20L
        val session1 = CinemaSessionMapper.toModel(CinemaSessionCreateDto(countOfPlaces = 10, startSessionDate = now.plusMinutes(50)), now, delayTime)
        val session2 = CinemaSessionMapper.toModel(CinemaSessionCreateDto(countOfPlaces = 15, startSessionDate = now.plusMinutes(50)), now, delayTime)
        val placesWithStatus1 = SessionUtil.reservePlaces(session1, listOf(1, 2, 3, 4, 5, 6, 7, 8), "category1", 1, sessionOptions(), true)
        val placesWithStatus2 = SessionUtil.reservePlaces(session2, listOf(1, 2), "category2", 2, sessionOptions(), true)
        return mutableMapOf(
            session1.id to session1.copy(freePlaces = placesWithStatus1.freePlaces, reservedPlaces = placesWithStatus1.reservedPlaces, totalIncome = placesWithStatus1.income),
            session2.id to session2.copy(freePlaces = placesWithStatus2.freePlaces, reservedPlaces = placesWithStatus2.reservedPlaces, totalIncome = placesWithStatus2.income)
        )
    }

    /**
     * Метод для проверки введённых в application.properties данных
     * */
    private fun validateDiscounts() {
        when {
            discount1 > 100 || discount1 < 0 -> throw ValidationException("Скидка для категории 1 меньше 0 или больше 100")
            discount2 > 100 || discount2 < 0 -> throw ValidationException("Скидка для категории 2 меньше 0 или больше 100")
            discount3 > 100 || discount3 < 0 -> throw ValidationException("Скидка для категории 3 меньше 0 или больше 100")
            minProfit > 100 || minProfit < 0 -> throw ValidationException("Процент минимального дохода с сеанса меньше 0 или больше 100")
        }
    }

}