package ru.avtamonov.social.cinema.dto

data class SessionOptions(
    val discount1: Int,
    val discount2: Int,
    val discount3: Int,
    val delayTimeForStandardCategory: Long,
    val minProfit: Int,
    val paybackTime: Long
)
