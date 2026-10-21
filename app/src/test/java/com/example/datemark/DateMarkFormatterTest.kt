package com.example.datemark

import com.example.datemark.domain.DateMarkCalculator
import com.example.datemark.domain.DateMarkFormatter
import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.DateMark
import com.example.datemark.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateMarkFormatterTest {

    // 38. Locale-aware formatting
    @Test
    fun testLocaleAwareFormatting() {
        val date = LocalDate.of(2026, 11, 15)

        val usMedium = DateMarkFormatter.formatDate(date, AppDateFormat.MEDIUM, Locale.US)
        val frMedium = DateMarkFormatter.formatDate(date, AppDateFormat.MEDIUM, Locale.FRANCE)

        // Both are locale-aware representations
        assertTrue(usMedium.isNotBlank())
        assertTrue(frMedium.isNotBlank())
        assertTrue(usMedium.contains("Nov") || usMedium.contains("15") || usMedium.contains("2026"))
    }

    @Test
    fun testMonthAndDayBadge() {
        val date = LocalDate.of(2026, 11, 15)
        val monthBadge = DateMarkFormatter.formatMonthBadge(date, Locale.US)
        val dayBadge = DateMarkFormatter.formatDayBadge(date)

        assertEquals("NOV", monthBadge)
        assertEquals("15", dayBadge)
    }

    @Test
    fun testFormatShareText() {
        val today = LocalDate.of(2026, 10, 21)
        val dateMark = DateMark(
            id = "1",
            label = "Birthday",
            date = LocalDate.of(2026, 11, 15),
            note = "Family birthday",
            recurrence = Recurrence.YEARLY,
            createdAt = today,
            updatedAt = today
        )
        val result = DateMarkCalculator.calculate(dateMark, today)
        val shareText = DateMarkFormatter.formatShareText(dateMark, result, AppDateFormat.LONG, Locale.US)

        assertTrue(shareText.contains("DateMark"))
        assertTrue(shareText.contains("Birthday"))
        assertTrue(shareText.contains("25 days left"))
        assertTrue(shareText.contains("Every year"))
        assertTrue(shareText.contains("Family birthday"))
    }

    @Test
    fun testFormatShareTextForPast() {
        val today = LocalDate.of(2026, 10, 21)
        val dateMark = DateMark(
            id = "2",
            label = "Driving License",
            date = LocalDate.of(2026, 9, 10),
            note = "",
            recurrence = Recurrence.NONE,
            createdAt = today,
            updatedAt = today
        )
        val result = DateMarkCalculator.calculate(dateMark, today)
        val shareText = DateMarkFormatter.formatShareText(dateMark, result, AppDateFormat.MEDIUM, Locale.US)

        assertTrue(shareText.contains("41 days ago"))
        assertTrue(shareText.contains("Does not repeat"))
    }

    @Test
    fun testFormatShareTextForToday() {
        val today = LocalDate.of(2026, 10, 21)
        val dateMark = DateMark(
            id = "3",
            label = "Event",
            date = today,
            note = "",
            recurrence = Recurrence.NONE,
            createdAt = today,
            updatedAt = today
        )
        val result = DateMarkCalculator.calculate(dateMark, today)
        val shareText = DateMarkFormatter.formatShareText(dateMark, result, AppDateFormat.MEDIUM, Locale.US)

        assertTrue(shareText.contains("Today"))
    }
}
