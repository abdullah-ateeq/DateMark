package com.example.datemark.domain

import com.example.datemark.model.DateFilter
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateMarkResult
import com.example.datemark.model.DateSort
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import java.time.LocalDate
import kotlin.math.abs

data class CalculatedDateMarkItem(
    val dateMark: DateMark,
    val result: DateMarkResult,
    val formattedDate: String,
    val dayBadgeMonth: String,
    val dayBadgeDay: String,
    val statusText: String,
    val recurrenceText: String
)

data class DateMarkSummary(
    val totalCount: Int,
    val upcomingCount: Int,
    val todayCount: Int,
    val passedCount: Int,
    val nextItem: CalculatedDateMarkItem?
)

object DateMarkFilterSorter {

    /**
     * Filters and sorts the list of date marks, returning calculated items ready for UI presentation.
     */
    fun process(
        dates: List<DateMark>,
        today: LocalDate,
        searchQuery: String,
        filter: DateFilter,
        sort: DateSort
    ): List<CalculatedDateMarkItem> {
        val calculatedItems = dates.map { dateMark ->
            val result = DateMarkCalculator.calculate(dateMark, today)
            CalculatedDateMarkItem(
                dateMark = dateMark,
                result = result,
                formattedDate = DateMarkFormatter.formatDate(result.targetDate),
                dayBadgeMonth = DateMarkFormatter.formatMonthBadge(result.targetDate),
                dayBadgeDay = DateMarkFormatter.formatDayBadge(result.targetDate),
                statusText = DateMarkFormatter.formatDayDifference(result.dayDifference),
                recurrenceText = DateMarkFormatter.formatRecurrence(dateMark.recurrence)
            )
        }

        // 1. Search Filter
        val query = searchQuery.trim()
        val searchedItems = if (query.isEmpty()) {
            calculatedItems
        } else {
            calculatedItems.filter { item ->
                item.dateMark.label.contains(query, ignoreCase = true) ||
                    item.dateMark.note.contains(query, ignoreCase = true)
            }
        }

        // 2. Category Filter
        val filteredItems = when (filter) {
            DateFilter.ALL -> searchedItems
            DateFilter.UPCOMING -> searchedItems.filter { it.result.status == DateStatus.UPCOMING }
            DateFilter.TODAY -> searchedItems.filter { it.result.status == DateStatus.TODAY }
            DateFilter.PASSED -> searchedItems.filter {
                it.result.status == DateStatus.PASSED && it.dateMark.recurrence == Recurrence.NONE
            }
            DateFilter.YEARLY -> searchedItems.filter { it.dateMark.recurrence == Recurrence.YEARLY }
        }

        // 3. Sorting
        return when (sort) {
            DateSort.NEAREST -> filteredItems.sortedWith(
                compareBy<CalculatedDateMarkItem> { item ->
                    // Today and future (>= 0) come first (group 0), past (< 0) comes second (group 1)
                    if (item.result.dayDifference >= 0) 0 else 1
                }.thenBy { item ->
                    if (item.result.dayDifference >= 0) {
                        item.result.dayDifference
                    } else {
                        // For past dates, most recently passed first (e.g. -1 before -10)
                        abs(item.result.dayDifference)
                    }
                }
            )
            DateSort.FURTHEST -> filteredItems.sortedWith(
                compareBy<CalculatedDateMarkItem> { item ->
                    if (item.result.dayDifference >= 0) 0 else 1
                }.thenByDescending { item ->
                    if (item.result.dayDifference >= 0) {
                        item.result.dayDifference
                    } else {
                        abs(item.result.dayDifference)
                    }
                }
            )
            DateSort.NAME_AZ -> filteredItems.sortedBy { it.dateMark.label.lowercase() }
            DateSort.NAME_ZA -> filteredItems.sortedByDescending { it.dateMark.label.lowercase() }
            DateSort.RECENTLY_ADDED -> filteredItems.sortedWith(
                compareByDescending<CalculatedDateMarkItem> { it.dateMark.createdAt }
                    .thenByDescending { it.dateMark.updatedAt }
            )
        }
    }

    /**
     * Computes summary metrics for the given dates.
     */
    fun computeSummary(
        dates: List<DateMark>,
        today: LocalDate
    ): DateMarkSummary {
        val calculated = dates.map { dateMark ->
            val result = DateMarkCalculator.calculate(dateMark, today)
            CalculatedDateMarkItem(
                dateMark = dateMark,
                result = result,
                formattedDate = DateMarkFormatter.formatDate(result.targetDate),
                dayBadgeMonth = DateMarkFormatter.formatMonthBadge(result.targetDate),
                dayBadgeDay = DateMarkFormatter.formatDayBadge(result.targetDate),
                statusText = DateMarkFormatter.formatDayDifference(result.dayDifference),
                recurrenceText = DateMarkFormatter.formatRecurrence(dateMark.recurrence)
            )
        }

        var upcomingCount = 0
        var todayCount = 0
        var passedCount = 0

        for (item in calculated) {
            when (item.result.status) {
                DateStatus.UPCOMING -> upcomingCount++
                DateStatus.TODAY -> todayCount++
                DateStatus.PASSED -> {
                    if (item.dateMark.recurrence == Recurrence.NONE) {
                        passedCount++
                    } else {
                        // Yearly recurring dates resolved next occurrence count as upcoming
                        upcomingCount++
                    }
                }
            }
        }

        // Find next item (today if exists, or closest upcoming)
        val todayItem = calculated.firstOrNull { it.result.status == DateStatus.TODAY }
        val nextItem = todayItem ?: calculated
            .filter { it.result.status == DateStatus.UPCOMING }
            .minByOrNull { it.result.dayDifference }

        return DateMarkSummary(
            totalCount = dates.size,
            upcomingCount = upcomingCount,
            todayCount = todayCount,
            passedCount = passedCount,
            nextItem = nextItem
        )
    }
}
