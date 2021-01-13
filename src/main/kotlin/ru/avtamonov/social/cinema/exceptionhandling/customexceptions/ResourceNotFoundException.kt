package ru.avtamonov.social.cinema.exceptionhandling.customexceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class ResourceNotFoundException(message: String) : RuntimeException(message)