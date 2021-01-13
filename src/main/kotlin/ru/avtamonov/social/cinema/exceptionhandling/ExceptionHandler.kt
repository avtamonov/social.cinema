package ru.avtamonov.social.cinema.exceptionhandling

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ResourceNotFoundException
import ru.avtamonov.social.cinema.exceptionhandling.customexceptions.ValidationException
import java.time.LocalDateTime
import java.util.*

@RestController
@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<Any> {
        return buildResponse(ex, HttpStatus.EXPECTATION_FAILED, request)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationFailedException(ex: ValidationException, request: WebRequest): ResponseEntity<Any> {
        return buildResponse(ex, HttpStatus.PRECONDITION_FAILED, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request)
    }

    private fun buildResponse(ex: Exception, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        return buildMessage(ex)
                .also { log.error(it, ex) }
                .let { CustomExceptionResponse(Date(), it, request.getDescription(false)) }
                .let { ResponseEntity(it, status) }
    }

    private fun buildMessage(ex: Exception) =
            ex.message ?: CustomExceptionResponse.MESSAGE_UNDEFINED

    /**
     * Метод перехватывающий ошибки валидации данных приходящих с фронта
     * @param ex MethodArgumentNotValidException - информация о невалидных данных
     * @param headers HttpHeaders - заголовки ответа
     * @param status HttpStatus - статус ответа (обычно всегда 500) поэтому меняется под более подходящий
     * @param request WebRequest - объект запроса (контекст запроса)
     * @return ResponseEntity<Any> - тело ответа
     */
    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val body = mutableMapOf<String, Any>()
        body["timestamp"] = LocalDateTime.now()
        body["status"] = status.value()
        body["errors"] = ex.bindingResult.fieldErrors.map { it.defaultMessage }
        return ResponseEntity(body, headers, status)
    }
}