package com.example.smartscholr.validation

object InputValidators {

    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9._-]{3,32}$")

    fun validateUsername(raw: String?): ValidationResult {
        val s = raw?.trim().orEmpty()
        return when {
            s.isEmpty() -> ValidationResult.Invalid.FieldEmpty
            !USERNAME_REGEX.matches(s) -> ValidationResult.Invalid.UsernameFormat
            else -> ValidationResult.Valid
        }
    }

    fun validatePassword(raw: String?): ValidationResult {
        val s = raw.orEmpty()
        return when {
            s.isEmpty() -> ValidationResult.Invalid.FieldEmpty
            s.length < 8 -> ValidationResult.Invalid.PasswordWeak
            else -> ValidationResult.Valid
        }
    }

    fun validateDisplayName(raw: String?): ValidationResult {
        val s = raw?.trim().orEmpty()
        return when {
            s.isEmpty() -> ValidationResult.Invalid.FieldEmpty
            s.length > 80 -> ValidationResult.Invalid.DisplayNameTooLong
            else -> ValidationResult.Valid
        }
    }

    fun validateOptionalEmail(raw: String?): ValidationResult {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty()) return ValidationResult.Valid
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        return if (emailRegex.matches(s)) ValidationResult.Valid else ValidationResult.Invalid.EmailInvalid
    }

    /** Returns a positive amount, or null if empty/invalid/not positive. */
    fun parsePositiveMoney(raw: String?): Double? {
        val normalized = raw?.trim()?.replace(",", "") ?: return null
        if (normalized.isEmpty()) return null
        return try {
            val d = normalized.toDouble()
            if (d.isNaN() || d.isInfinite() || d <= 0) null else d
        } catch (_: NumberFormatException) {
            null
        }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()

    sealed class Invalid : ValidationResult() {
        object FieldEmpty : Invalid()
        object UsernameFormat : Invalid()
        object PasswordWeak : Invalid()
        object EmailInvalid : Invalid()
        object DisplayNameTooLong : Invalid()
        object MoneyRequired : Invalid()
        object MoneyInvalid : Invalid()
    }
}
