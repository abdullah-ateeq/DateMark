package com.example.datemark.domain

import com.example.datemark.model.DateMark
import com.example.datemark.model.DateMarkResult
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateMarkCalculator {

    /**
     * Calculates the exact calendar day difference between [today] and [targetDate].
     * Returns positive if [targetDate] is in the future, 0 if today, negative if in the past.
     */
    fun calculateDaysDifference(
        today: LocalDate,
        targetDate: LocalDate
    ): Long {
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    /**
     * Returns the [DateStatus] based on the day difference between [today] and [targetDate].
     */
    fun getStatus(
        today: LocalDate,
        targetDate: LocalDate
    ): DateStatus {
        val days = calculateDaysDifference(today, targetDate)
        return when {
            days > 0L -> DateStatus.UPCOMING
            days == 0L -> DateStatus.TODAY
            else -> DateStatus.PASSED
        }
    }

    /**
     * Resolves the effective target date for a [DateMark], taking into account yearly recurrence.
     */
    fun getEffectiveTargetDate(
        dateMark: DateMark,
        today: LocalDate
    ): LocalDate {
        return if (dateMark.recurrence == Recurrence.YEARLY) {
            RecurrenceCalculator.getNextYearlyOccurrence(
                month = dateMark.date.month,
                day = dateMark.date.dayOfMonth,
                today = today
            )
        } else {
            dateMark.date
        }
    }

    /**
     * Full calculation resulting in a [DateMarkResult].
     */
    fun calculate(
        dateMark: DateMark,
        today: LocalDate
    ): DateMarkResult {
        val targetDate = getEffectiveTargetDate(dateMark, today)
        val diff = calculateDaysDifference(today, targetDate)
        val status = getStatus(today, targetDate)
        return DateMarkResult(
            targetDate = targetDate,
            today = today,
            dayDifference = diff,
            status = status
        )
    }
}
