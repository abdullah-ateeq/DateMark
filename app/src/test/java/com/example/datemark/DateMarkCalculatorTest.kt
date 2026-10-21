package com.example.datemark

import com.example.datemark.domain.DateMarkCalculator
import com.example.datemark.domain.DateMarkFormatter
import com.example.datemark.domain.RecurrenceCalculator
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class DateMarkCalculatorTest {

    // 1. Future date calculation
    @Test
    fun testFutureDateCalculation() {
        val today = LocalDate.of(2026, 10, 21)
        val target = LocalDate.of(2026, 11, 15)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(25L, days)

        val formatted = DateMarkFormatter.formatDayDifference(days)
        assertEquals("25 DAYS LEFT", formatted)
    }

    // 2. Past date calculation
    @Test
    fun testPastDateCalculation() {
        val today = LocalDate.of(2026, 10, 21)
        val target = LocalDate.of(2026, 9, 10)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(-41L, days)

        val formatted = DateMarkFormatter.formatDayDifference(days)
        assertEquals("41 DAYS AGO", formatted)
        // Must not expose negative sign
        assertNotEquals("-41 DAYS", formatted)
    }

    // 3. Today detection
    @Test
    fun testTodayDetection() {
        val today = LocalDate.of(2026, 10, 21)
        val target = LocalDate.of(2026, 10, 21)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(0L, days)

        val status = DateMarkCalculator.getStatus(today, target)
        assertEquals(DateStatus.TODAY, status)

        val formatted = DateMarkFormatter.formatDayDifference(days)
        assertEquals("TODAY", formatted)
        assertNotEquals("0 DAYS LEFT", formatted)
    }

    // 4. One-day difference
    @Test
    fun testOneDayDifference() {
        val today = LocalDate.of(2026, 10, 20)
        val tomorrow = LocalDate.of(2026, 10, 21)
        val yesterday = LocalDate.of(2026, 10, 19)

        val daysTomorrow = DateMarkCalculator.calculateDaysDifference(today, tomorrow)
        assertEquals(1L, daysTomorrow)
        assertEquals("1 DAY LEFT", DateMarkFormatter.formatDayDifference(daysTomorrow))

        val daysYesterday = DateMarkCalculator.calculateDaysDifference(today, yesterday)
        assertEquals(-1L, daysYesterday)
        assertEquals("1 DAY AGO", DateMarkFormatter.formatDayDifference(daysYesterday))
    }

    // 5. Month boundary
    @Test
    fun testMonthBoundary() {
        val today = LocalDate.of(2027, 1, 31)
        val target = LocalDate.of(2027, 2, 1)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(1L, days)
        assertEquals("1 DAY LEFT", DateMarkFormatter.formatDayDifference(days))
    }

    // 6. Year boundary
    @Test
    fun testYearBoundary() {
        val today = LocalDate.of(2026, 12, 31)
        val target = LocalDate.of(2027, 1, 1)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(1L, days)
        assertEquals("1 DAY LEFT", DateMarkFormatter.formatDayDifference(days))
    }

    // 7. Leap year calculation
    @Test
    fun testLeapYear() {
        val today = LocalDate.of(2028, 2, 28)
        val target = LocalDate.of(2028, 2, 29)
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(1L, days)
        assertEquals("1 DAY LEFT", DateMarkFormatter.formatDayDifference(days))
    }

    // 8. February 29 in leap and non-leap years
    @Test
    fun testFebruary29Resolution() {
        // In leap year 2028
        val leapYearDate = RecurrenceCalculator.resolveOccurrenceForYear(Month.FEBRUARY, 29, 2028)
        assertEquals(LocalDate.of(2028, 2, 29), leapYearDate)

        // In non-leap year 2027
        val nonLeapYearDate = RecurrenceCalculator.resolveOccurrenceForYear(Month.FEBRUARY, 29, 2027)
        assertEquals(LocalDate.of(2027, 2, 28), nonLeapYearDate)
    }

    // 9. Large future dates (no integer overflow)
    @Test
    fun testLargeFutureDate() {
        val today = LocalDate.of(2026, 1, 1)
        val target = LocalDate.of(2126, 1, 1) // 100 years into future
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(36524L, days) // Year 2100 is not a leap year (24 leap years)
        assertEquals(DateStatus.UPCOMING, DateMarkCalculator.getStatus(today, target))
    }

    // 10. Large past dates (no integer overflow)
    @Test
    fun testLargePastDate() {
        val today = LocalDate.of(2026, 1, 1)
        val target = LocalDate.of(1926, 1, 1) // 100 years in past
        val days = DateMarkCalculator.calculateDaysDifference(today, target)
        assertEquals(-36525L, days) // Year 2000 was a leap year (25 leap years)
        assertEquals(DateStatus.PASSED, DateMarkCalculator.getStatus(today, target))
    }

    // 15. Date status check
    @Test
    fun testDateStatus() {
        val today = LocalDate.of(2026, 10, 21)
        assertEquals(DateStatus.UPCOMING, DateMarkCalculator.getStatus(today, LocalDate.of(2026, 10, 22)))
        assertEquals(DateStatus.TODAY, DateMarkCalculator.getStatus(today, LocalDate.of(2026, 10, 21)))
        assertEquals(DateStatus.PASSED, DateMarkCalculator.getStatus(today, LocalDate.of(2026, 10, 20)))
    }
}
