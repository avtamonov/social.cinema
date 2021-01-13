package ru.avtamonov.social.cinema.config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ClockConfig {

    @Value("\${timeZone}")
    val timeZone: String = ""

    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of(timeZone))
}
