package ru.avtamonov.social.cinema.exceptionhandling.customexceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class ValidationException(message: String) : RuntimeException(message)