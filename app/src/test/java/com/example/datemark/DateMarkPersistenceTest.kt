package com.example.datemark

import android.content.SharedPreferences
import com.example.datemark.data.DateRepository
import com.example.datemark.data.SettingsRepository
import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.AppSettings
import com.example.datemark.model.AppTheme
import com.example.datemark.model.DateMark
import com.example.datemark.model.DateSort
import com.example.datemark.model.Recurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FakeSharedPreferences : SharedPreferences {
    val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(this)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
        private val tempMap = mutableMapOf<String, Any?>()
        private val removeKeys = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            key?.let { tempMap[it] = value }
            return this
        }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            key?.let { tempMap[it] = values }
            return this
        }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            key?.let { tempMap[it] = value }
            return this
        }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            key?.let { tempMap[it] = value }
            return this
        }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            key?.let { tempMap[it] = value }
            return this
        }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            key?.let { tempMap[it] = value }
            return this
        }
        override fun remove(key: String?): SharedPreferences.Editor {
            key?.let { removeKeys.add(it) }
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }
        override fun commit(): Boolean {
            apply()
            return true
        }
        override fun apply() {
            if (clearAll) prefs.map.clear()
            removeKeys.forEach { prefs.map.remove(it) }
            prefs.map.putAll(tempMap)
        }
    }
}

class DateMarkPersistenceTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var dateRepository: DateRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        dateRepository = DateRepository(fakePrefs)
        settingsRepository = SettingsRepository(fakePrefs)
    }

    // 32. Edit date (updates existing record, no duplicate created)
    @Test
    fun testEditDateUpdatesExistingRecord() {
        val originalDate = DateMark(
            id = "test-id-1",
            label = "Birthday",
            date = LocalDate.of(2026, 11, 15),
            note = "Original note",
            recurrence = Recurrence.YEARLY,
            createdAt = LocalDate.of(2026, 1, 1),
            updatedAt = LocalDate.of(2026, 1, 1)
        )
        dateRepository.save(originalDate)
        assertEquals(1, dateRepository.getAll().size)

        // Edit existing record
        val editedDate = originalDate.copy(
            label = "Birthday (Updated)",
            date = LocalDate.of(2026, 11, 20),
            updatedAt = LocalDate.of(2026, 10, 21)
        )
        dateRepository.save(editedDate)

        val dates = dateRepository.getAll()
        assertEquals(1, dates.size) // No duplicate!
        val saved = dates.first()
        assertEquals("Birthday (Updated)", saved.label)
        assertEquals(LocalDate.of(2026, 11, 20), saved.date)
        assertEquals(LocalDate.of(2026, 1, 1), saved.createdAt) // Preserved
        assertEquals(LocalDate.of(2026, 10, 21), saved.updatedAt)
    }

    // 33. Delete date
    @Test
    fun testDeleteDate() {
        val date1 = DateMark("id-1", "Date 1", LocalDate.of(2026, 11, 15), "", Recurrence.NONE, LocalDate.now(), LocalDate.now())
        val date2 = DateMark("id-2", "Date 2", LocalDate.of(2026, 12, 1), "", Recurrence.NONE, LocalDate.now(), LocalDate.now())
        dateRepository.save(date1)
        dateRepository.save(date2)
        assertEquals(2, dateRepository.getAll().size)

        dateRepository.delete("id-1")
        val remaining = dateRepository.getAll()
        assertEquals(1, remaining.size)
        assertEquals("id-2", remaining.first().id)
    }

    // 34. Persistence across app restart
    @Test
    fun testPersistenceAcrossRestart() {
        val dateMark = DateMark(
            id = "stable-id-123",
            label = "Anniversary",
            date = LocalDate.of(2026, 12, 25),
            note = "Silver anniversary celebration",
            recurrence = Recurrence.YEARLY,
            createdAt = LocalDate.of(2026, 5, 10),
            updatedAt = LocalDate.of(2026, 5, 10)
        )
        dateRepository.save(dateMark)

        // Simulate app restart by creating a new repository instance pointing to same storage
        val newRepositoryInstance = DateRepository(fakePrefs)
        val restoredList = newRepositoryInstance.getAll()

        assertEquals(1, restoredList.size)
        val restored = restoredList.first()
        assertEquals("stable-id-123", restored.id)
        assertEquals("Anniversary", restored.label)
        assertEquals(LocalDate.of(2026, 12, 25), restored.date)
        assertEquals("Silver anniversary celebration", restored.note)
        assertEquals(Recurrence.YEARLY, restored.recurrence)
        assertEquals(LocalDate.of(2026, 5, 10), restored.createdAt)
        assertEquals(LocalDate.of(2026, 5, 10), restored.updatedAt)
    }

    // 35. Clear all dates
    @Test
    fun testClearAllDates() {
        val date1 = DateMark("1", "D1", LocalDate.now(), "", Recurrence.NONE, LocalDate.now(), LocalDate.now())
        val date2 = DateMark("2", "D2", LocalDate.now(), "", Recurrence.NONE, LocalDate.now(), LocalDate.now())
        dateRepository.save(date1)
        dateRepository.save(date2)
        assertEquals(2, dateRepository.getAll().size)

        dateRepository.clearAll()
        assertTrue(dateRepository.getAll().isEmpty())
    }

    // 36. Settings persistence
    @Test
    fun testSettingsPersistence() {
        val customSettings = AppSettings(
            theme = AppTheme.DARK,
            dynamicColor = false,
            hapticFeedback = false,
            animations = false,
            confirmDelete = false,
            dateFormat = AppDateFormat.LONG,
            defaultSort = DateSort.NAME_AZ
        )
        settingsRepository.saveSettings(customSettings)

        // Simulate restart
        val newSettingsRepo = SettingsRepository(fakePrefs)
        val loaded = newSettingsRepo.getSettings()

        assertEquals(AppTheme.DARK, loaded.theme)
        assertFalse(loaded.dynamicColor)
        assertFalse(loaded.hapticFeedback)
        assertFalse(loaded.animations)
        assertFalse(loaded.confirmDelete)
        assertEquals(AppDateFormat.LONG, loaded.dateFormat)
        assertEquals(DateSort.NAME_AZ, loaded.defaultSort)
    }

    // 37. Settings reset
    @Test
    fun testSettingsReset() {
        // Change settings
        settingsRepository.saveSettings(
            AppSettings(
                theme = AppTheme.DARK,
                dynamicColor = false,
                confirmDelete = false,
                defaultSort = DateSort.NAME_ZA
            )
        )

        // Also save a date to ensure date is preserved on settings reset
        dateRepository.save(DateMark("keep-me", "Keep Me", LocalDate.now(), "", Recurrence.NONE, LocalDate.now(), LocalDate.now()))

        // Reset settings
        val reset = settingsRepository.resetSettings()

        assertEquals(AppTheme.SYSTEM, reset.theme)
        assertTrue(reset.dynamicColor)
        assertTrue(reset.hapticFeedback)
        assertTrue(reset.animations)
        assertTrue(reset.confirmDelete)
        assertEquals(AppDateFormat.SYSTEM_DEFAULT, reset.dateFormat)
        assertEquals(DateSort.NEAREST, reset.defaultSort)

        // Dates remain intact
        assertEquals(1, dateRepository.getAll().size)
    }
}
