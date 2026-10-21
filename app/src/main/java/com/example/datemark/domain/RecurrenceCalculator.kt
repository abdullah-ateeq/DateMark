package com.example.datemark.domain

import java.time.LocalDate
import java.time.Month
import java.time.Year

object RecurrenceCalculator {

    /**
     * Resolves the calendar date for a given month and day in the specified year.
     * If the target is February 29 and the year is not a leap year, February 28 is used.
     */
    fun resolveOccurrenceForYear(month: Month, day: Int, year: Int): LocalDate {
        return if (month == Month.FEBRUARY && day == 29) {
            if (Year.isLeap(year.toLong())) {
                LocalDate.of(year, Month.FEBRUARY, 29)
            } else {
                LocalDate.of(year, Month.FEBRUARY, 28)
            }
        } else {
            LocalDate.of(year, month, day)
        }
    }

    /**
     * Computes the next occurrence of a yearly recurring date relative to [today].
     * If the occurrence for the current year is today or in the future, it is returned.
     * If the occurrence for the current year has already passed (is before today),
     * the occurrence for the next year is returned.
     */
    fun getNextYearlyOccurrence(
        month: Month,
        day: Int,
        today: LocalDate
    ): LocalDate {
        val thisYearOccurrence = resolveOccurrenceForYear(month, day, today.year)
        return if (thisYearOccurrence.isBefore(today)) {
            resolveOccurrenceForYear(month, day, today.year + 1)
        } else {
            thisYearOccurrence
        }
    }
}
