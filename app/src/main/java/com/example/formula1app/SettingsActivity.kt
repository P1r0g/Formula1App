package com.example.formula1app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.formula1app.ui.theme.Formula1AppTheme

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                SettingsScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onBack: () -> Unit
) {
    val darkTheme = AppState.darkTheme ?: isSystemInDarkTheme()

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
                    onSave = { updatedProfile ->
                        AppState.userProfile = updatedProfile
                    }
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
                                AppState.darkTheme = checked
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
                                AppState.languageCode = "ru"
                            }
                        )

                        LanguageOption(
                            label = stringResource(R.string.language_english),
                            selected = AppState.languageCode == "en",
                            onSelect = {
                                AppState.languageCode = "en"
                            }
                        )
                    }
                }
            }
            BackupSettingsCard()

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