package com.example.formula1app.ui

import android.util.Patterns
import com.example.formula1app.R

object Validation {

    private val loginPattern = Regex("[A-Za-z0-9_]{3,20}")

    fun emailError(value: String): Int? {
        val email = value.trim()

        return when {
            email.isEmpty() ->
                R.string.error_email_required

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                R.string.error_email_invalid

            else -> null
        }
    }

    fun loginError(value: String): Int? {
        val login = value.trim()

        return when {
            login.isEmpty() ->
                R.string.error_login_required

            !loginPattern.matches(login) ->
                R.string.error_login_invalid

            else -> null
        }
    }

    fun passwordError(value: String): Int? {
        return when {
            value.isBlank() ->
                R.string.error_password_required

            value.length < 8 ->
                R.string.error_password_short

            else -> null
        }
    }

    fun confirmationError(
        password: String,
        confirmation: String
    ): Int? {
        return when {
            confirmation.isBlank() ->
                R.string.error_confirmation_required

            confirmation != password ->
                R.string.error_password_mismatch

            else -> null
        }
    }
}