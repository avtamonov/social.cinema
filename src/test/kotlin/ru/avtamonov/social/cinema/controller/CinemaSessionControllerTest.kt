package ru.avtamonov.social.cinema.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.CinemaSessionResponse
import ru.avtamonov.social.cinema.service.CinemaSessionService
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@WebMvcTest(CinemaSessionController::class)
@RunWith(SpringRunner::class)
class CinemaSessionControllerTest {

    @Autowired
    lateinit var cinemaSessionService: CinemaSessionService
    @Autowired
    lateinit var mockMvc: MockMvc

    private val baseUrl = "/v1/session"

    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    private val resp = CinemaSessionResponse(
        id = UUID.randomUUID(),
        startSessionDate = LocalDateTime.now(clock),
        dateCreate =  LocalDateTime.now(clock),
        startReserveForStandardCategory =  LocalDateTime.now(clock)
    )

    @Test
    fun `when createCinemaSession should return 200`() {
        every {
            cinemaSessionService.createCinemaSession(ofType(CinemaSessionCreateDto::class))
        } returns resp
        val request = CinemaSessionCreateDto(countOfPlaces = 0, startSessionDate = LocalDateTime.now(clock))
        mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper().writeValueAsString(request)))
                .andExpect(status().isOk)
    }

    private fun objectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.dateFormat = StdDateFormat().withColonInTimeZone(true)
        return objectMapper
    }

    @TestConfiguration
    class Mocks {
        @Bean
        fun cinemaSessionService() = mockk<CinemaSessionService>()
    }
}