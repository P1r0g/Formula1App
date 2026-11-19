package com.example.formula1app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.formula1app.data.AppState
import java.text.DateFormat
import java.util.Date

@Composable
fun BackupSettingsCard() {
    val backup = AppState.standingsBackup
    val hasData = AppState.standings.isNotEmpty()
    val locale = LocalConfiguration.current.locales[0]

    var message by remember {
        mutableStateOf<Int?>(null)
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
                text = stringResource(R.string.backup_title),
                style = MaterialTheme.typography.titleMedium
            )

            if (backup == null) {
                Text(
                    text = stringResource(R.string.backup_missing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val createdAt = remember(
                    backup.createdAtMillis,
                    locale
                ) {
                    DateFormat.getDateTimeInstance(
                        DateFormat.SHORT,
                        DateFormat.SHORT,
                        locale
                    ).format(Date(backup.createdAtMillis))
                }

                Text(
                    text = stringResource(
                        R.string.backup_info,
                        backup.standings.size,
                        createdAt
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!hasData) {
                Text(
                    text = stringResource(R.string.backup_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = hasData,
                onClick = {
                    message = if (AppState.createBackup()) {
                        R.string.backup_saved
                    } else {
                        R.string.backup_no_data
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (backup == null) {
                            R.string.create_backup_action
                        } else {
                            R.string.update_backup_action
                        }
                    )
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = backup != null,
                onClick = {
                    message = if (AppState.restoreBackup()) {
                        R.string.backup_restored
                    } else {
                        R.string.backup_missing
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        R.string.restore_backup_action
                    )
                )
            }

            TextButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = backup != null,
                onClick = {
                    AppState.deleteBackup()
                    message = R.string.backup_deleted
                }
            ) {
                Text(
                    text = stringResource(
                        R.string.delete_backup_action
                    )
                )
            }

            message?.let { messageId ->
                Text(
                    text = stringResource(messageId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}