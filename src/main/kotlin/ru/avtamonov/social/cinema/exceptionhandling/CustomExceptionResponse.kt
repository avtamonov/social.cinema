package ru.avtamonov.social.cinema.exceptionhandling

import java.util.*

class CustomExceptionResponse(
        val timestamp: Date,
        val message: String?,
        val details: String?
) {
    companion object {
        const val MESSAGE_UNDEFINED: String = "Cannot define exception message, watch logs on your server"
    }
}