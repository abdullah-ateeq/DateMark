package com.example.datemark.model

import java.time.LocalDate

data class DateMarkResult(
    val targetDate: LocalDate,
    val today: LocalDate,
    val dayDifference: Long,
    val status: DateStatus
)
