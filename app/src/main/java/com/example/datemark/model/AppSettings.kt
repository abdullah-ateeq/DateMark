package com.example.datemark.model

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val dateFormat: AppDateFormat = AppDateFormat.SYSTEM_DEFAULT,
    val defaultSort: DateSort = DateSort.NEAREST
)
