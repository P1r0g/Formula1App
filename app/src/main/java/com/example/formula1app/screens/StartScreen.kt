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
import com.example.formula1app.ui.Validation
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    initialLogin: String,
    onOpenRegistration: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var login by rememberSaveable { mutableStateOf(initialLogin) }
    var password by remember { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var authenticationError by rememberSaveable { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    var signingIn by remember { mutableStateOf(false) }

    val loginError = if (showErrors) Validation.loginError(login) else null
    val passwordError = if (showErrors) Validation.passwordError(password) else null

    val signIn: () -> Unit = {
        showErrors = true
        authenticationError = null

        val fieldsAreValid =
            Validation.loginError(login) == null &&
                    Validation.passwordError(password) == null

        if (fieldsAreValid && !signingIn) {
            val enteredLogin = login.trim()
            val enteredPassword = password
            signingIn = true

            scope.launch {
                try {
                    when {
                        AppState.userProfile == null -> {
                            authenticationError = R.string.error_registration_required
                        }

                        AppState.matchesCredentials(enteredLogin, enteredPassword) -> {
                            onLoginSuccess()
                        }

                        else -> {
                            authenticationError = R.string.error_invalid_credentials
                        }
                    }
                } finally {
                    signingIn = false
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
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(text = stringResource(R.string.start_description))

            OutlinedTextField(
                value = login,
                enabled = !signingIn,
                onValueChange = {
                    login = it
                    authenticationError = null
                },
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
                enabled = !signingIn,
                onValueChange = {
                    password = it
                    authenticationError = null
                },
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

            authenticationError?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = signIn,
                enabled = !signingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.login_action))
            }

            TextButton(
                onClick = onOpenRegistration,
                enabled = !signingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.registration_action))
            }
        }
    }
}