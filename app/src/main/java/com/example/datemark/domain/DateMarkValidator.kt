package com.example.datemark.domain

object DateMarkValidator {
    const val MAX_LABEL_LENGTH = 100
    const val MAX_NOTE_LENGTH = 500

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    fun validate(label: String, note: String = ""): ValidationResult {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Please enter a label.")
        }
        if (trimmed.length > MAX_LABEL_LENGTH) {
            return ValidationResult.Error("Label cannot exceed $MAX_LABEL_LENGTH characters.")
        }
        if (note.length > MAX_NOTE_LENGTH) {
            return ValidationResult.Error("Note cannot exceed $MAX_NOTE_LENGTH characters.")
        }
        return ValidationResult.Valid
    }
}
