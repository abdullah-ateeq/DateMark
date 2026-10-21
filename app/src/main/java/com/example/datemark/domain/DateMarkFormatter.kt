package com.example.datemark.domain

import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateMarkResult
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs

object DateMarkFormatter {

    /**
     * Formats a [LocalDate] according to the selected [AppDateFormat] and [Locale].
     * Never hardcodes date patterns, respecting system or locale settings.
     */
    fun formatDate(
        date: LocalDate,
        format: AppDateFormat = AppDateFormat.SYSTEM_DEFAULT,
        locale: Locale = Locale.getDefault()
    ): String {
        val style = when (format) {
            AppDateFormat.SYSTEM_DEFAULT -> FormatStyle.MEDIUM
            AppDateFormat.SHORT -> FormatStyle.SHORT
            AppDateFormat.MEDIUM -> FormatStyle.MEDIUM
            AppDateFormat.LONG -> FormatStyle.LONG
        }
        val formatter = DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)
        return date.format(formatter)
    }

    /**
     * Formats the day badge month string (e.g. "NOV") in uppercase for calendar badge display.
     */
    fun formatMonthBadge(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val formatter = DateTimeFormatter.ofPattern("MMM", locale)
        return date.format(formatter).uppercase(locale)
    }

    /**
     * Formats the day badge day string (e.g. "15").
     */
    fun formatDayBadge(date: LocalDate): String {
        return date.dayOfMonth.toString()
    }

    /**
     * Formats day difference into user-facing status string:
     * - Future: "X DAYS LEFT" or "1 DAY LEFT"
     * - Today: "TODAY"
     * - Past: "X DAYS AGO" or "1 DAY AGO"
     * Never exposes negative numbers or "0 DAYS LEFT".
     */
    fun formatDayDifference(dayDifference: Long): String {
        return when {
            dayDifference > 1L -> "$dayDifference DAYS LEFT"
            dayDifference == 1L -> "1 DAY LEFT"
            dayDifference == 0L -> "TODAY"
            dayDifference == -1L -> "1 DAY AGO"
            else -> "${abs(dayDifference)} DAYS AGO"
        }
    }

    /**
     * Formats day difference for copy/share text (e.g. "25 days left", "Today", "41 days ago").
     */
    fun formatDayDifferenceText(dayDifference: Long): String {
        return when {
            dayDifference > 1L -> "$dayDifference days left"
            dayDifference == 1L -> "1 day left"
            dayDifference == 0L -> "Today"
            dayDifference == -1L -> "1 day ago"
            else -> "${abs(dayDifference)} days ago"
        }
    }

    /**
     * Formats recurrence description.
     */
    fun formatRecurrence(recurrence: Recurrence): String {
        return when (recurrence) {
            Recurrence.NONE -> "Does not repeat"
            Recurrence.YEARLY -> "Every year"
        }
    }

    /**
     * Prepares full copy/share representation of a date mark.
     */
    fun formatShareText(
        dateMark: DateMark,
        result: DateMarkResult,
        format: AppDateFormat = AppDateFormat.SYSTEM_DEFAULT,
        locale: Locale = Locale.getDefault()
    ): String {
        val formattedDate = formatDate(result.targetDate, format, locale)
        val dayStatus = formatDayDifferenceText(result.dayDifference)
        val recurrenceText = formatRecurrence(dateMark.recurrence)

        val sb = StringBuilder()
        sb.append("DateMark\n\n")
        sb.append(dateMark.label).append("\n\n")
        sb.append("Date:\n").append(formattedDate).append("\n\n")
        sb.append(dayStatus).append("\n\n")
        sb.append("Repeats:\n").append(recurrenceText)

        if (dateMark.note.isNotBlank()) {
            sb.append("\n\nNote:\n").append(dateMark.note.trim())
        }

        return sb.toString()
    }
}
