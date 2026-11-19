package com.example.formula1app

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.formula1app.data.UserProfile

@Composable
fun ProfileSettingsCard(
    profile: UserProfile,
    onSave: (UserProfile) -> Unit
) {
    var email by rememberSaveable(profile.email) {
        mutableStateOf(profile.email)
    }

    var login by rememberSaveable(profile.login) {
        mutableStateOf(profile.login)
    }

    var newPassword by rememberSaveable {
        mutableStateOf("")
    }

    var confirmation by rememberSaveable {
        mutableStateOf("")
    }

    var showErrors by rememberSaveable {
        mutableStateOf(false)
    }

    var saved by rememberSaveable {
        mutableStateOf(false)
    }

    val changingPassword =
        newPassword.isNotEmpty() || confirmation.isNotEmpty()

    val emailError = if (showErrors) {
        Validation.emailError(email)
    } else {
        null
    }

    val loginError = if (showErrors) {
        Validation.loginError(login)
    } else {
        null
    }

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
                onValueChange = {
                    email = it
                    saved = false
                },
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
                onValueChange = {
                    login = it
                    saved = false
                },
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
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.new_password_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(stringResource(passwordError))
                    } else {
                        Text(
                            stringResource(R.string.password_change_hint)
                        )
                    }
                }
            )

            OutlinedTextField(
                value = confirmation,
                onValueChange = {
                    confirmation = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.confirmation_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmationError != null,
                supportingText = {
                    confirmationError?.let {
                        Text(stringResource(it))
                    }
                }
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showErrors = true
                    saved = false

                    val shouldChangePassword =
                        newPassword.isNotEmpty() ||
                                confirmation.isNotEmpty()

                    val errors = listOf(
                        Validation.emailError(email),
                        Validation.loginError(login),
                        if (shouldChangePassword) {
                            Validation.passwordError(newPassword)
                        } else {
                            null
                        },
                        if (shouldChangePassword) {
                            Validation.confirmationError(
                                newPassword,
                                confirmation
                            )
                        } else {
                            null
                        }
                    )

                    if (errors.all { it == null }) {
                        val updatedProfile = profile.copy(
                            email = email.trim(),
                            login = login.trim(),
                            password = if (shouldChangePassword) {
                                newPassword
                            } else {
                                profile.password
                            }
                        )

                        onSave(updatedProfile)

                        newPassword = ""
                        confirmation = ""
                        showErrors = false
                        saved = true
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.save_profile_action)
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