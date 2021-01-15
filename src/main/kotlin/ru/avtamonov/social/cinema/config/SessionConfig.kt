package ru.avtamonov.social.cinema.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.avtamonov.social.cinema.dto.SessionOptions
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException

@Configuration
class SessionConfig {

    @Value("\${social-discount-1}")
    private var discount1 = 0

    @Value("\${social-discount-2}")
    private var discount2 = 0

    @Value("\${social-discount-3}")
    private var discount3 = 0

    @Value("\${delay-time-for-standard-category}")
    private var delayTimeForStandardCategory = 0L

    @Bean
    fun sessionOptions(): SessionOptions {
        validateDiscounts()
        return SessionOptions(
            discount1 = discount1,
            discount2 = discount2,
            discount3 = discount3,
            delayTimeForStandardCategory = delayTimeForStandardCategory
        )
    }

    private fun validateDiscounts() {
        when {
            discount1 > 100 || discount1 < 0 -> throw ValidationException("Скидка для категории 1 меньше 0 или больше 100")
            discount2 > 100 || discount2 < 0 -> throw ValidationException("Скидка для категории 2 меньше 0 или больше 100")
            discount3 > 100 || discount3 < 0 -> throw ValidationException("Скидка для категории 3 меньше 0 или больше 100")
        }
    }

}