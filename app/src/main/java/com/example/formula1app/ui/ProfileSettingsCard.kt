package com.example.formula1app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.formula1app.data.UserProfile
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
fun ProfileSettingsCard(
    profile: UserProfile,
    onSave: suspend (UserProfile, String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }

    var email by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    var login by rememberSaveable(profile.login) { mutableStateOf(profile.login) }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }

    val changingPassword = newPassword.isNotEmpty() || confirmation.isNotEmpty()
    val emailError = if (showErrors) Validation.emailError(email) else null
    val loginError = if (showErrors) Validation.loginError(login) else null
    val passwordError = if (showErrors && changingPassword) {
        Validation.passwordError(newPassword)
    } else {
        null
    }
    val confirmationError = if (showErrors && changingPassword) {
        Validation.confirmationError(newPassword, confirmation)
    } else {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = email,
                enabled = !saving,
                onValueChange = {
                    email = it
                    saved = false
                },
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
                onValueChange = {
                    login = it
                    saved = false
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
                value = newPassword,
                enabled = !saving,
                onValueChange = {
                    newPassword = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.new_password_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    Text(stringResource(passwordError ?: R.string.password_change_hint))
                }
            )

            OutlinedTextField(
                value = confirmation,
                enabled = !saving,
                onValueChange = {
                    confirmation = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.confirmation_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmationError != null,
                supportingText = {
                    confirmationError?.let { Text(stringResource(it)) }
                }
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving,
                onClick = {
                    showErrors = true
                    saved = false
                    saveFailed = false

                    val shouldChangePassword =
                        newPassword.isNotEmpty() || confirmation.isNotEmpty()

                    val errors = listOf(
                        Validation.emailError(email),
                        Validation.loginError(login),
                        if (shouldChangePassword) {
                            Validation.passwordError(newPassword)
                        } else {
                            null
                        },
                        if (shouldChangePassword) {
                            Validation.confirmationError(newPassword, confirmation)
                        } else {
                            null
                        }
                    )

                    if (errors.all { it == null } && !saving) {
                        val updatedProfile = profile.copy(
                            email = email.trim(),
                            login = login.trim()
                        )
                        val password = newPassword.takeIf { shouldChangePassword }
                        saving = true

                        scope.launch {
                            try {
                                onSave(updatedProfile, password)
                                newPassword = ""
                                confirmation = ""
                                showErrors = false
                                saved = true
                            } catch (_: IOException) {
                                saveFailed = true
                            } finally {
                                saving = false
                            }
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.save_profile_action))
            }

            if (saveFailed) {
                Text(
                    text = stringResource(R.string.error_profile_save_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (saved) {
                Text(
                    text = stringResource(R.string.profile_saved),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}