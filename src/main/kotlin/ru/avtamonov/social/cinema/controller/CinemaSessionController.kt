package ru.avtamonov.social.cinema.controller

import io.swagger.annotations.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.avtamonov.social.cinema.dto.CinemaSessionCreateDto
import ru.avtamonov.social.cinema.dto.CinemaSessionResponse
import ru.avtamonov.social.cinema.dto.ReserveRequest
import ru.avtamonov.social.cinema.dto.TransferRequest
import ru.avtamonov.social.cinema.enum.Status
import ru.avtamonov.social.cinema.service.CinemaSessionService
import java.util.*

@CrossOrigin
@RestController
@Api(tags = ["Сеанс"])
@RequestMapping(path = ["/v1/session"], produces = [MediaType.APPLICATION_JSON_VALUE])
@ApiResponses(
    ApiResponse(code = 200, message = "Операция успешно выполнена"),
    ApiResponse(code = 400, message = "Неверный запрос")
)
class CinemaSessionController (
    private val cinemaSessionService: CinemaSessionService
) {
    @PostMapping
    @ApiOperation("Создать сеанс", response = CinemaSessionResponse::class)
    fun createCinemaSession(
        @ApiParam("Сеанс") @RequestBody request: CinemaSessionCreateDto
    ): CinemaSessionResponse {
        return cinemaSessionService.createCinemaSession(request)
    }

    @PostMapping("/transfer")
    @ApiOperation("Перенести сеанс", response = Status::class)
    fun transferSessionTime(
        @ApiParam("Сеанс") @RequestBody request: TransferRequest
    ): Status {
        return cinemaSessionService.transferSessionTime(request)
    }

    @PostMapping("/reserve")
    @ApiOperation("Забронитовать места на сеансе", response = CinemaSessionResponse::class)
    fun reserveSeatsOnCinemaSession(
        @ApiParam("Места для бронирования") @RequestBody request: ReserveRequest
    ): CinemaSessionResponse {
        return cinemaSessionService.reserveTicketOnSession(request)
    }

    @DeleteMapping("/{id}")
    @ApiOperation("Удалить сеанс")
    fun deleteCinemaSession(
        @ApiParam("Сеанс") @PathVariable id: UUID
    ) {
        cinemaSessionService.deleteCinemaSession(id)
    }

    @GetMapping
    @ApiOperation("Получить все сеансы", response = CinemaSessionResponse::class)
    fun getCinemaSession(): List<CinemaSessionResponse> {
        return cinemaSessionService.getCinemaSessions()
    }
}