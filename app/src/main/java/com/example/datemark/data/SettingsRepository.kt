package com.example.datemark.data

import android.content.Context
import android.content.SharedPreferences
import com.example.datemark.model.AppDateFormat
import com.example.datemark.model.AppSettings
import com.example.datemark.model.AppTheme
import com.example.datemark.model.DateSort

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun getSettings(): AppSettings {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true)
        val animations = prefs.getBoolean(KEY_ANIMATIONS, true)
        val confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true)

        val dateFormatName = prefs.getString(KEY_DATE_FORMAT, AppDateFormat.SYSTEM_DEFAULT.name)
            ?: AppDateFormat.SYSTEM_DEFAULT.name
        val dateFormat = try { AppDateFormat.valueOf(dateFormatName) } catch (e: Exception) { AppDateFormat.SYSTEM_DEFAULT }

        val sortName = prefs.getString(KEY_DEFAULT_SORT, DateSort.NEAREST.name)
            ?: DateSort.NEAREST.name
        val defaultSort = try { DateSort.valueOf(sortName) } catch (e: Exception) { DateSort.NEAREST }

        return AppSettings(
            theme = theme,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animations = animations,
            confirmDelete = confirmDelete,
            dateFormat = dateFormat,
            defaultSort = defaultSort
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putString(KEY_DATE_FORMAT, settings.dateFormat.name)
            .putString(KEY_DEFAULT_SORT, settings.defaultSort.name)
            .apply()
    }

    fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        return defaults
    }

    companion object {
        private const val PREFS_NAME = "datemark_settings_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_DATE_FORMAT = "date_format"
        private const val KEY_DEFAULT_SORT = "default_sort"
    }
}
