package com.example.datemark.model

import com.example.datemark.domain.CalculatedDateMarkItem
import com.example.datemark.domain.DateMarkSummary
import java.time.LocalDate

data class DateMarkUiState(
    val dates: List<DateMark> = emptyList(),
    val searchQuery: String = "",
    val filter: DateFilter = DateFilter.ALL,
    val sort: DateSort = DateSort.NEAREST,
    val selectedDate: DateMark? = null,
    val editingDateId: String? = null,
    val labelInput: String = "",
    val selectedDateInput: LocalDate = LocalDate.now(),
    val noteInput: String = "",
    val recurrence: Recurrence = Recurrence.NONE,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val displayItems: List<CalculatedDateMarkItem> = emptyList(),
    val summary: DateMarkSummary = DateMarkSummary(0, 0, 0, 0, null),
    val selectedDetailItem: CalculatedDateMarkItem? = null,
    val settings: AppSettings = AppSettings()
)
