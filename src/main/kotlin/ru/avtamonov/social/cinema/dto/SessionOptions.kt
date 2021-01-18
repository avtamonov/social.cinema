package ru.avtamonov.social.cinema.dto

data class SessionOptions(
    val discount1: Int = 10,
    val discount2: Int = 15,
    val discount3: Int = 20,
    val delayTimeForStandardCategory: Long = 10,
    val minProfit: Int = 50,
    val paybackTime: Long = 20
)
