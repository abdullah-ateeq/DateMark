package com.example.datemark.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.datemark.data.DateRepository
import com.example.datemark.data.SettingsRepository
import com.example.datemark.domain.DateMarkCalculator
import com.example.datemark.domain.DateMarkFilterSorter
import com.example.datemark.domain.DateMarkFormatter
import com.example.datemark.domain.DateMarkValidator
import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.AppSettings
import com.example.datemark.model.AppTheme
import com.example.datemark.model.DateFilter
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateMarkUiState
import com.example.datemark.model.DateSort
import com.example.datemark.model.Recurrence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class DateMarkViewModel(
    application: Application,
    private val dateRepository: DateRepository,
    private val settingsRepository: SettingsRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val fixedToday: LocalDate? = null
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        dateRepository = DateRepository(application),
        settingsRepository = SettingsRepository(application),
        zoneId = ZoneId.systemDefault(),
        fixedToday = null
    )

    private val _uiState = MutableStateFlow(DateMarkUiState())
    val uiState: StateFlow<DateMarkUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun getToday(): LocalDate {
        return fixedToday ?: LocalDate.now(zoneId)
    }

    private fun loadInitialData() {
        val settings = settingsRepository.getSettings()
        val dates = dateRepository.getAll()
        val today = getToday()

        val displayItems = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = "",
            filter = DateFilter.ALL,
            sort = settings.defaultSort
        )
        val summary = DateMarkFilterSorter.computeSummary(dates, today)

        _uiState.update {
            it.copy(
                dates = dates,
                sort = settings.defaultSort,
                filter = DateFilter.ALL,
                searchQuery = "",
                displayItems = displayItems,
                summary = summary,
                settings = settings,
                selectedDateInput = today
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        val today = getToday()
        _uiState.update { current ->
            val displayItems = DateMarkFilterSorter.process(
                dates = current.dates,
                today = today,
                searchQuery = query,
                filter = current.filter,
                sort = current.sort
            )
            current.copy(
                searchQuery = query,
                displayItems = displayItems
            )
        }
    }

    fun onFilterChange(filter: DateFilter) {
        val today = getToday()
        _uiState.update { current ->
            val displayItems = DateMarkFilterSorter.process(
                dates = current.dates,
                today = today,
                searchQuery = current.searchQuery,
                filter = filter,
                sort = current.sort
            )
            current.copy(
                filter = filter,
                displayItems = displayItems
            )
        }
    }

    fun onSortChange(sort: DateSort) {
        val today = getToday()
        _uiState.update { current ->
            val displayItems = DateMarkFilterSorter.process(
                dates = current.dates,
                today = today,
                searchQuery = current.searchQuery,
                filter = current.filter,
                sort = sort
            )
            current.copy(
                sort = sort,
                displayItems = displayItems
            )
        }
    }

    fun onSelectDateMark(id: String) {
        val today = getToday()
        val dateMark = _uiState.value.dates.firstOrNull { it.id == id }
        val detailItem = if (dateMark != null) {
            val result = DateMarkCalculator.calculate(dateMark, today)
            DateMarkFilterSorter.process(
                dates = listOf(dateMark),
                today = today,
                searchQuery = "",
                filter = DateFilter.ALL,
                sort = DateSort.NEAREST
            ).firstOrNull()
        } else null

        _uiState.update {
            it.copy(
                selectedDate = dateMark,
                selectedDetailItem = detailItem
            )
        }
    }

    fun prepareAddDate() {
        val today = getToday()
        _uiState.update {
            it.copy(
                editingDateId = null,
                labelInput = "",
                selectedDateInput = today,
                noteInput = "",
                recurrence = Recurrence.NONE,
                errorMessage = null,
                isSaving = false
            )
        }
    }

    fun prepareEditDate(id: String) {
        val dateMark = _uiState.value.dates.firstOrNull { it.id == id }
        if (dateMark != null) {
            _uiState.update {
                it.copy(
                    editingDateId = dateMark.id,
                    labelInput = dateMark.label,
                    selectedDateInput = dateMark.date,
                    noteInput = dateMark.note,
                    recurrence = dateMark.recurrence,
                    errorMessage = null,
                    isSaving = false
                )
            }
        }
    }

    fun onLabelChange(label: String) {
        _uiState.update { it.copy(labelInput = label, errorMessage = null) }
    }

    fun onSelectedDateChange(date: LocalDate) {
        _uiState.update { it.copy(selectedDateInput = date, errorMessage = null) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(noteInput = note, errorMessage = null) }
    }

    fun onRecurrenceChange(recurrence: Recurrence) {
        _uiState.update { it.copy(recurrence = recurrence, errorMessage = null) }
    }

    fun saveDate(onSuccess: () -> Unit = {}) {
        val current = _uiState.value
        val validation = DateMarkValidator.validate(current.labelInput, current.noteInput)
        if (validation is DateMarkValidator.ValidationResult.Error) {
            _uiState.update { it.copy(errorMessage = validation.message) }
            return
        }

        val today = getToday()
        val editingId = current.editingDateId

        val dateMark = if (editingId != null) {
            val existing = current.dates.firstOrNull { it.id == editingId }
            DateMark(
                id = editingId,
                label = current.labelInput.trim(),
                date = current.selectedDateInput,
                note = current.noteInput.trim(),
                recurrence = current.recurrence,
                createdAt = existing?.createdAt ?: today,
                updatedAt = today
            )
        } else {
            DateMark(
                id = UUID.randomUUID().toString(),
                label = current.labelInput.trim(),
                date = current.selectedDateInput,
                note = current.noteInput.trim(),
                recurrence = current.recurrence,
                createdAt = today,
                updatedAt = today
            )
        }

        dateRepository.save(dateMark)
        refreshDatesList()
        onSelectDateMark(dateMark.id)
        onSuccess()
    }

    fun deleteDate(id: String, onSuccess: () -> Unit = {}) {
        dateRepository.delete(id)
        _uiState.update { it.copy(selectedDate = null, selectedDetailItem = null) }
        refreshDatesList()
        onSuccess()
    }

    fun clearAllDates() {
        dateRepository.clearAll()
        _uiState.update { it.copy(selectedDate = null, selectedDetailItem = null) }
        refreshDatesList()
    }

    private fun refreshDatesList() {
        val dates = dateRepository.getAll()
        val today = getToday()
        val current = _uiState.value

        val displayItems = DateMarkFilterSorter.process(
            dates = dates,
            today = today,
            searchQuery = current.searchQuery,
            filter = current.filter,
            sort = current.sort
        )
        val summary = DateMarkFilterSorter.computeSummary(dates, today)

        _uiState.update {
            it.copy(
                dates = dates,
                displayItems = displayItems,
                summary = summary
            )
        }
    }

    fun updateTheme(theme: AppTheme) {
        val updated = _uiState.value.settings.copy(theme = theme)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(dynamicColor = enabled)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(hapticFeedback = enabled)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateAnimations(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(animations = enabled)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateConfirmDelete(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(confirmDelete = enabled)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun updateDateFormat(format: AppDateFormat) {
        val updated = _uiState.value.settings.copy(dateFormat = format)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
        refreshDatesList()
    }

    fun updateDefaultSort(sort: DateSort) {
        val updated = _uiState.value.settings.copy(defaultSort = sort)
        settingsRepository.saveSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun resetSettings() {
        val defaults = settingsRepository.resetSettings()
        _uiState.update { it.copy(settings = defaults, sort = defaults.defaultSort) }
        refreshDatesList()
    }

    fun getShareText(id: String): String? {
        val dateMark = _uiState.value.dates.firstOrNull { it.id == id } ?: return null
        val today = getToday()
        val result = DateMarkCalculator.calculate(dateMark, today)
        return DateMarkFormatter.formatShareText(
            dateMark = dateMark,
            result = result,
            format = _uiState.value.settings.dateFormat
        )
    }

    fun getCopyText(id: String): String? {
        return getShareText(id)
    }
}
