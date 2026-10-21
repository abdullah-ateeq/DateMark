package com.example.datemark

import com.example.datemark.domain.DateMarkCalculator
import com.example.datemark.domain.RecurrenceCalculator
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class RecurrenceCalculatorTest {

    // 11. Yearly recurrence
    @Test
    fun testYearlyRecurrence() {
        val today = LocalDate.of(2026, 10, 21)
        val nextOccurrence = RecurrenceCalculator.getNextYearlyOccurrence(Month.NOVEMBER, 15, today)
        assertEquals(LocalDate.of(2026, 11, 15), nextOccurrence)

        val days = DateMarkCalculator.calculateDaysDifference(today, nextOccurrence)
        assertEquals(25L, days)
    }

    // 12. Passed yearly recurrence advances to next year
    @Test
    fun testPassedYearlyRecurrence() {
        val today = LocalDate.of(2026, 11, 20)
        // 15 November has passed for 2026, so next occurrence should be 2027
        val nextOccurrence = RecurrenceCalculator.getNextYearlyOccurrence(Month.NOVEMBER, 15, today)
        assertEquals(LocalDate.of(2027, 11, 15), nextOccurrence)

        val days = DateMarkCalculator.calculateDaysDifference(today, nextOccurrence)
        assertEquals(360L, days)
    }

    // 13. Current yearly occurrence on today stays today
    @Test
    fun testYearlyDateOnToday() {
        val today = LocalDate.of(2026, 11, 15)
        // Must stay today, do not advance directly to 2027
        val occurrence = RecurrenceCalculator.getNextYearlyOccurrence(Month.NOVEMBER, 15, today)
        assertEquals(LocalDate.of(2026, 11, 15), occurrence)

        val status = DateMarkCalculator.getStatus(today, occurrence)
        assertEquals(DateStatus.TODAY, status)
    }

    // 14. February 29 yearly recurrence in leap vs non-leap years
    @Test
    fun testFebruary29YearlyRecurrence() {
        // Saved: 29 February Every Year
        // Today is in 2026 after Feb: next occurrence should resolve in 2027 (non-leap -> Feb 28)
        val today2026 = LocalDate.of(2026, 10, 21)
        val next2027 = RecurrenceCalculator.getNextYearlyOccurrence(Month.FEBRUARY, 29, today2026)
        assertEquals(LocalDate.of(2027, 2, 28), next2027)

        // Today is in 2027 after Feb: next occurrence should resolve in 2028 (leap year -> Feb 29)
        val today2027 = LocalDate.of(2027, 3, 1)
        val next2028 = RecurrenceCalculator.getNextYearlyOccurrence(Month.FEBRUARY, 29, today2027)
        assertEquals(LocalDate.of(2028, 2, 29), next2028)
    }
}
