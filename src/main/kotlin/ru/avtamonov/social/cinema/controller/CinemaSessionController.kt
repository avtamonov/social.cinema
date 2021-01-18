package ru.avtamonov.social.cinema.controller

import io.swagger.annotations.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.avtamonov.social.cinema.dto.*
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
    @ApiOperation("Забронировать места на сеансе", response = CinemaSessionResponse::class)
    fun reserveSeatsOnCinemaSession(
        @ApiParam("Места для бронирования") @RequestBody request: ReserveRequest,
        @ApiParam("Логин") @RequestParam login: String,
        @ApiParam("Категория клиента", allowableValues = "0, 1, 2, 3") @RequestParam clientCategory: Int
    ): CinemaSessionResponse {
        return cinemaSessionService.reservePlacesOnSession(request, login, clientCategory)
    }

    @PostMapping("/discount-mode")
    @ApiOperation("Вкл/выкл скидки", response = DiscountMode::class)
    fun workWithDiscountMode(
        @ApiParam("Скидки вкл", defaultValue = "true") @RequestParam discount: Boolean
    ): DiscountMode {
        return cinemaSessionService.setDiscountMode(discount)
    }

    @PutMapping("/reserve")
    @ApiOperation("Отменить бронирование мест на сеансе", response = CinemaSessionResponse::class)
    fun unReserveSeatsOnCinemaSession(
        @ApiParam("Места для отмены бронирования") @RequestBody request: ReserveRequest,
        @ApiParam("Логин") @RequestParam login: String
    ): CinemaSessionResponse {
        return cinemaSessionService.unReservePlacesOnSession(request, login)
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

    @GetMapping("/history")
    @ApiOperation("Прочитать историю посещений", response = SessionHistoryResponse::class)
    fun getCinemaSessionHistory(
        @ApiParam("Логин") @RequestParam login: String
    ): List<SessionHistoryResponse> {
        return cinemaSessionService.getSessionHistoryByLogin(login)
    }

    @GetMapping("/reserved")
    @ApiOperation("Просмотреть информацию о забронированных местах", response = SessionWithReservedPlaces::class)
    fun getReservedInfo(
        @ApiParam("Логин") @RequestParam login: String
    ): List<SessionWithReservedPlaces> {
        return cinemaSessionService.getReservedPlacesByLogin(login)
    }
}