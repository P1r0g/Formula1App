package com.example.formula1app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.formula1app.R
import com.example.formula1app.data.StandingsFileStatus
import com.example.formula1app.data.StandingsViewModel
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun StandingsFileCard(standingsViewModel: StandingsViewModel) {
    val standingsState by standingsViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    var status by remember { mutableStateOf<StandingsFileStatus?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    suspend fun update(action: suspend () -> Unit = {}) {
        if (busy) return
        busy = true
        failed = false

        try {
            val error = runCatching { action() }.exceptionOrNull()
            if (error is CancellationException) throw error
            failed = error != null
            status = standingsViewModel.readFileStatus()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            status = null
            failed = true
        } finally {
            busy = false
        }
    }

    LaunchedEffect(standingsState.isLoading) {
        if (!standingsState.isLoading) update()
    }

    val info = status?.file
    val hasBackup = status?.hasBackup == true
    val canEdit = !busy && !standingsState.isLoading

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.export_file_title),
                style = MaterialTheme.typography.titleMedium
            )

            if (info != null) {
                Text(stringResource(R.string.export_file_info, info.path, info.sizeBytes))

                if (info.modifiedAtMillis > 0) {
                    val date = remember(info.modifiedAtMillis, locale) {
                        DateFormat.getDateTimeInstance(
                            DateFormat.SHORT,
                            DateFormat.SHORT,
                            locale
                        ).format(Date(info.modifiedAtMillis))
                    }
                    Text(stringResource(R.string.export_file_modified, date))
                }
            } else if (status != null) {
                Text(stringResource(R.string.export_file_missing))
            }

            if (hasBackup) {
                Text(stringResource(R.string.export_backup_available))
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit && info != null,
                onClick = {
                    scope.launch { update(standingsViewModel::deleteExportedFile) }
                }
            ) {
                Text(stringResource(R.string.export_file_delete))
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = canEdit && hasBackup,
                onClick = {
                    scope.launch { update(standingsViewModel::restoreExportedFile) }
                }
            ) {
                Text(stringResource(R.string.export_file_restore))
            }

            if (failed) {
                Text(
                    text = stringResource(R.string.export_file_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}