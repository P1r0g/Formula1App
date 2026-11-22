package com.example.formula1app.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.formula1app.R
import com.example.formula1app.data.AppState
import com.example.formula1app.ui.BackupSettingsCard
import com.example.formula1app.ui.ProfileSettingsCard
import com.example.formula1app.data.StandingsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.io.IOException
import kotlinx.coroutines.launch
import com.example.formula1app.ui.StandingsFileCard

@Composable
fun SettingsScreen(
    standingsViewModel: StandingsViewModel,
    onBack: () -> Unit
) {
    val darkTheme = AppState.darkTheme ?: isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    var saveFailed by remember { mutableStateOf(false) }

    fun saveAppearance(save: suspend () -> Unit) {
        saveFailed = false
        scope.launch {
            try {
                save()
            } catch (_: IOException) {
                saveFailed = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_action),
                style = MaterialTheme.typography.headlineMedium
            )

            val profile = AppState.userProfile

            if (profile != null) {
                ProfileSettingsCard(
                    profile = profile,
                    onSave = AppState::saveProfile
                )
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
                        text = stringResource(R.string.appearance_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dark_theme_label),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { checked ->
                                saveAppearance { AppState.updateDarkTheme(checked) }
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.language_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        LanguageOption(
                            label = stringResource(R.string.language_russian),
                            selected = AppState.languageCode == "ru",
                            onSelect = {
                                saveAppearance { AppState.updateLanguage("ru") }
                            }
                        )

                        LanguageOption(
                            label = stringResource(R.string.language_english),
                            selected = AppState.languageCode == "en",
                            onSelect = {
                                saveAppearance { AppState.updateLanguage("en") }
                            }
                        )
                    }
                }
            }
            if (saveFailed) {
                Text(
                    text = stringResource(R.string.error_appearance_save_failed),
                    color = MaterialTheme.colorScheme.error
                )
            }
            StandingsFileCard(standingsViewModel)
            BackupSettingsCard(standingsViewModel)

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(
                        R.string.back_to_standings_action
                    )
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}