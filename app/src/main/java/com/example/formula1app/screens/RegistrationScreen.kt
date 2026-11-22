package com.example.formula1app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.formula1app.R
import com.example.formula1app.data.AppState
import com.example.formula1app.data.UserProfile
import com.example.formula1app.ui.Validation
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onRegistrationComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var login by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }

    val emailError = if (showErrors) Validation.emailError(email) else null
    val loginError = if (showErrors) Validation.loginError(login) else null
    val passwordError = if (showErrors) Validation.passwordError(password) else null
    val confirmationError = if (showErrors) {
        Validation.confirmationError(password, confirmation)
    } else {
        null
    }

    val registerUser: () -> Unit = {
        showErrors = true

        val errors = listOf(
            Validation.emailError(email),
            Validation.loginError(login),
            Validation.passwordError(password),
            Validation.confirmationError(password, confirmation)
        )

        if (errors.all { it == null } && !saving) {
            val profile = UserProfile(email = email.trim(), login = login.trim())
            val enteredPassword = password
            saving = true
            saveFailed = false

            scope.launch {
                try {
                    AppState.saveProfile(profile, enteredPassword)
                    currentCoroutineContext().ensureActive()
                    onRegistrationComplete(profile.login)
                } catch (_: IOException) {
                    saveFailed = true
                } finally {
                    saving = false
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.registration_title),
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = email,
                enabled = !saving,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                isError = emailError != null,
                supportingText = {
                    emailError?.let { Text(stringResource(it)) }
                }
            )

            OutlinedTextField(
                value = login,
                enabled = !saving,
                onValueChange = { login = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii
                ),
                isError = loginError != null,
                supportingText = {
                    loginError?.let { Text(stringResource(it)) }
                }
            )

            OutlinedTextField(
                value = password,
                enabled = !saving,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let { Text(stringResource(it)) }
                }
            )

            OutlinedTextField(
                value = confirmation,
                enabled = !saving,
                onValueChange = { confirmation = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.confirmation_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                isError = confirmationError != null,
                supportingText = {
                    confirmationError?.let { Text(stringResource(it)) }
                }
            )

            if (saveFailed) {
                Text(
                    text = stringResource(R.string.error_profile_save_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = registerUser,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.register_action))
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.back_to_login_action))
            }
        }
    }
}