package com.example.datemark

import com.example.datemark.domain.DateMarkValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateMarkValidatorTest {

    // 28. Empty label validation
    @Test
    fun testEmptyLabelValidation() {
        val resultEmpty = DateMarkValidator.validate("")
        assertTrue(resultEmpty is DateMarkValidator.ValidationResult.Error)
        assertEquals(
            "Please enter a label.",
            (resultEmpty as DateMarkValidator.ValidationResult.Error).message
        )

        val resultWhitespace = DateMarkValidator.validate("   ")
        assertTrue(resultWhitespace is DateMarkValidator.ValidationResult.Error)
        assertEquals(
            "Please enter a label.",
            (resultWhitespace as DateMarkValidator.ValidationResult.Error).message
        )
    }

    // 29. Label maximum length (100 characters max)
    @Test
    fun testLabelMaximumLength() {
        val valid100 = "a".repeat(100)
        val result100 = DateMarkValidator.validate(valid100)
        assertTrue(result100 is DateMarkValidator.ValidationResult.Valid)

        val invalid101 = "a".repeat(101)
        val result101 = DateMarkValidator.validate(invalid101)
        assertTrue(result101 is DateMarkValidator.ValidationResult.Error)
        assertEquals(
            "Label cannot exceed 100 characters.",
            (result101 as DateMarkValidator.ValidationResult.Error).message
        )
    }

    // 30. Note maximum length (500 characters max)
    @Test
    fun testNoteMaximumLength() {
        val valid500 = "n".repeat(500)
        val result500 = DateMarkValidator.validate("Valid Label", valid500)
        assertTrue(result500 is DateMarkValidator.ValidationResult.Valid)

        val invalid501 = "n".repeat(501)
        val result501 = DateMarkValidator.validate("Valid Label", invalid501)
        assertTrue(result501 is DateMarkValidator.ValidationResult.Error)
        assertEquals(
            "Note cannot exceed 500 characters.",
            (result501 as DateMarkValidator.ValidationResult.Error).message
        )
    }

    @Test
    fun testUnicodeAndSpecialCharactersInLabel() {
        val unicodeLabel = "🎉 Anniversary & Birthday #1! 🎈 日本語"
        val result = DateMarkValidator.validate(unicodeLabel)
        assertTrue(result is DateMarkValidator.ValidationResult.Valid)
    }
}
