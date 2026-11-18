package com.example.formula1app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.formula1app.ui.theme.Formula1AppTheme

class RegistrationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var email by rememberSaveable { mutableStateOf("") }
            var login by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            var confirmation by rememberSaveable { mutableStateOf("") }
            var showErrors by rememberSaveable { mutableStateOf(false) }

            val emailError =
                if (showErrors) Validation.emailError(email) else null

            val loginError =
                if (showErrors) Validation.loginError(login) else null

            val passwordError =
                if (showErrors) Validation.passwordError(password) else null

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

                if (errors.all { it == null }) {
                    val profile = UserProfile(
                        email = email.trim(),
                        login = login.trim(),
                        password = password
                    )

                    AppState.userProfile = profile

                    val result = Intent()
                        .putExtra(EXTRA_LOGIN, profile.login)

                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }

            Formula1AppTheme {
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
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(stringResource(R.string.email_label))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            isError = emailError != null,
                            supportingText = {
                                emailError?.let {
                                    Text(stringResource(it))
                                }
                            }
                        )

                        OutlinedTextField(
                            value = login,
                            onValueChange = { login = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(stringResource(R.string.login_label))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii
                            ),
                            isError = loginError != null,
                            supportingText = {
                                loginError?.let {
                                    Text(stringResource(it))
                                }
                            }
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(stringResource(R.string.password_label))
                            },
                            singleLine = true,
                            visualTransformation =
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            isError = passwordError != null,
                            supportingText = {
                                passwordError?.let {
                                    Text(stringResource(it))
                                }
                            }
                        )

                        OutlinedTextField(
                            value = confirmation,
                            onValueChange = { confirmation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(stringResource(R.string.confirmation_label))
                            },
                            singleLine = true,
                            visualTransformation =
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            isError = confirmationError != null,
                            supportingText = {
                                confirmationError?.let {
                                    Text(stringResource(it))
                                }
                            }
                        )

                        Button(
                            onClick = registerUser,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.register_action))
                        }

                        TextButton(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.back_to_login_action))
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_LOGIN = "login"
    }
}