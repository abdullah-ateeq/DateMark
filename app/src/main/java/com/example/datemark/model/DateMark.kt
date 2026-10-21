package com.example.datemark.model

import java.time.LocalDate

data class DateMark(
    val id: String,
    val label: String,
    val date: LocalDate,
    val note: String,
    val recurrence: Recurrence,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
)
