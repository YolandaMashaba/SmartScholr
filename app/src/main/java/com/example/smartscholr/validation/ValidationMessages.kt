package com.example.smartscholr.validation

import androidx.annotation.StringRes
import com.example.smartscholr.R

@StringRes
fun ValidationResult.Invalid.resolve(): Int = when (this) {
    ValidationResult.Invalid.FieldEmpty -> R.string.validation_field_empty
    ValidationResult.Invalid.UsernameFormat -> R.string.validation_username_format
    ValidationResult.Invalid.PasswordWeak -> R.string.validation_password_weak
    ValidationResult.Invalid.EmailInvalid -> R.string.validation_email_invalid
    ValidationResult.Invalid.DisplayNameTooLong -> R.string.validation_display_name_long
    ValidationResult.Invalid.MoneyRequired -> R.string.validation_money_required
    ValidationResult.Invalid.MoneyInvalid -> R.string.validation_money_invalid
}
