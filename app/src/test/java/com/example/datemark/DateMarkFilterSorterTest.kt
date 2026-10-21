package com.example.datemark

import com.example.datemark.domain.DateMarkFilterSorter
import com.example.datemark.model.DateFilter
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateSort
import com.example.datemark.model.DateStatus
import com.example.datemark.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateMarkFilterSorterTest {

    private val today = LocalDate.of(2026, 10, 21)

    private val sampleDates = listOf(
        DateMark(
            id = "1",
            label = "Birthday",
            date = LocalDate.of(2026, 11, 15), // 25 days away
            note = "Family dinner",
            recurrence = Recurrence.YEARLY,
            createdAt = LocalDate.of(2026, 1, 1),
            updatedAt = LocalDate.of(2026, 1, 1)
        ),
        DateMark(
            id = "2",
            label = "Project Deadline",
            date = LocalDate.of(2026, 11, 30), // 40 days away
            note = "Final submission",
            recurrence = Recurrence.NONE,
            createdAt = LocalDate.of(2026, 2, 1),
            updatedAt = LocalDate.of(2026, 2, 1)
        ),
        DateMark(
            id = "3",
            label = "Team Meeting",
            date = LocalDate.of(2026, 10, 21), // Today
            note = "Sprint review",
            recurrence = Recurrence.NONE,
            createdAt = LocalDate.of(2026, 3, 1),
            updatedAt = LocalDate.of(2026, 3, 1)
        ),
        DateMark(
            id = "4",
            label = "License Renewal",
            date = LocalDate.of(2026, 9, 10), // 41 days ago
            note = "Driving license",
            recurrence = Recurrence.NONE,
            createdAt = LocalDate.of(2026, 4, 1),
            updatedAt = LocalDate.of(2026, 4, 1)
        )
    )

    // 16. Search by label
    @Test
    fun testSearchByLabel() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "birthday",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        assertEquals(1, result.size)
        assertEquals("Birthday", result.first().dateMark.label)
    }

    // 17. Search by note
    @Test
    fun testSearchByNote() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "submission",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        assertEquals(1, result.size)
        assertEquals("Project Deadline", result.first().dateMark.label)
    }

    // 18. Case-insensitive search
    @Test
    fun testCaseInsensitiveSearch() {
        val resultLower = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "project",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        val resultUpper = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "PROJECT",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        assertEquals(1, resultLower.size)
        assertEquals(1, resultUpper.size)
        assertEquals(resultLower.first().dateMark.id, resultUpper.first().dateMark.id)
    }

    // 19. Upcoming filter
    @Test
    fun testUpcomingFilter() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "",
            filter = DateFilter.UPCOMING,
            sort = DateSort.NEAREST
        )
        assertEquals(2, result.size)
        assertTrue(result.all { it.result.status == DateStatus.UPCOMING })
    }

    // 20. Today filter
    @Test
    fun testTodayFilter() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "",
            filter = DateFilter.TODAY,
            sort = DateSort.NEAREST
        )
        assertEquals(1, result.size)
        assertEquals("Team Meeting", result.first().dateMark.label)
        assertEquals(DateStatus.TODAY, result.first().result.status)
    }

    // 21. Passed filter
    @Test
    fun testPassedFilter() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "",
            filter = DateFilter.PASSED,
            sort = DateSort.NEAREST
        )
        assertEquals(1, result.size)
        assertEquals("License Renewal", result.first().dateMark.label)
        assertEquals(DateStatus.PASSED, result.first().result.status)
        assertEquals(Recurrence.NONE, result.first().dateMark.recurrence)
    }

    // 22. Yearly filter
    @Test
    fun testYearlyFilter() {
        val result = DateMarkFilterSorter.process(
            dates = sampleDates,
            today = today,
            searchQuery = "",
            filter = DateFilter.YEARLY,
            sort = DateSort.NEAREST
        )
        assertEquals(1, result.size)
        assertEquals("Birthday", result.first().dateMark.label)
        assertEquals(Recurrence.YEARLY, result.first().dateMark.recurrence)
    }

    // 23. Nearest sorting
    @Test
    fun testNearestSorting() {
        val dates = listOf(
            DateMark("1", "In 10 days", LocalDate.of(2026, 10, 31), "", Recurrence.NONE, today, today),
            DateMark("2", "In 2 days", LocalDate.of(2026, 10, 23), "", Recurrence.NONE, today, today),
            DateMark("3", "In 5 days", LocalDate.of(2026, 10, 26), "", Recurrence.NONE, today, today)
        )
        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        val diffs = result.map { it.result.dayDifference }
        assertEquals(listOf(2L, 5L, 10L), diffs)
    }

    // 24. Furthest sorting
    @Test
    fun testFurthestSorting() {
        val dates = listOf(
            DateMark("1", "In 10 days", LocalDate.of(2026, 10, 31), "", Recurrence.NONE, today, today),
            DateMark("2", "In 2 days", LocalDate.of(2026, 10, 23), "", Recurrence.NONE, today, today),
            DateMark("3", "In 5 days", LocalDate.of(2026, 10, 26), "", Recurrence.NONE, today, today)
        )
        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.FURTHEST
        )
        val diffs = result.map { it.result.dayDifference }
        assertEquals(listOf(10L, 5L, 2L), diffs)
    }

    // 25. Name A–Z
    @Test
    fun testNameAZSorting() {
        val dates = listOf(
            DateMark("1", "Charlie", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today),
            DateMark("2", "Alice", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today),
            DateMark("3", "Bob", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today)
        )
        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.NAME_AZ
        )
        assertEquals(listOf("Alice", "Bob", "Charlie"), result.map { it.dateMark.label })
    }

    // 26. Name Z–A
    @Test
    fun testNameZASorting() {
        val dates = listOf(
            DateMark("1", "Charlie", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today),
            DateMark("2", "Alice", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today),
            DateMark("3", "Bob", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, today, today)
        )
        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.NAME_ZA
        )
        assertEquals(listOf("Charlie", "Bob", "Alice"), result.map { it.dateMark.label })
    }

    // 27. Recently added
    @Test
    fun testRecentlyAddedSorting() {
        val dates = listOf(
            DateMark("1", "First", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
            DateMark("2", "Third", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1)),
            DateMark("3", "Second", LocalDate.of(2026, 11, 1), "", Recurrence.NONE, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 1))
        )
        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.RECENTLY_ADDED
        )
        assertEquals(listOf("Third", "Second", "First"), result.map { it.dateMark.label })
    }

    // 31. Duplicate calendar dates
    @Test
    fun testDuplicateCalendarDatesAllowed() {
        val date1 = DateMark("1", "Birthday", LocalDate.of(2026, 11, 15), "", Recurrence.NONE, today, today)
        val date2 = DateMark("2", "Anniversary", LocalDate.of(2026, 11, 15), "", Recurrence.NONE, today, today)
        val dates = listOf(date1, date2)

        val result = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = DateSort.NEAREST
        )
        assertEquals(2, result.size)
        assertNotEquals(result[0].dateMark.label, result[1].dateMark.label)
        assertEquals(result[0].result.targetDate, result[1].result.targetDate)
    }
}
