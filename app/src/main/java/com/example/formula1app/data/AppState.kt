package com.example.formula1app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {
    private lateinit var standingsStorage: StandingsStorage

    var userProfile: UserProfile? by mutableStateOf(null)

    var darkTheme: Boolean? by mutableStateOf(null)

    var languageCode: String by mutableStateOf("ru")

    var standingsBackup: StandingsBackup? by mutableStateOf(null)
        private set

    fun initializeStandingsStorage(context: Context) {
        if (::standingsStorage.isInitialized) return

        standingsStorage = StandingsStorage(context)
        standingsBackup = standingsStorage.readManualBackup()
    }

    fun createBackup(standings: List<DriverStanding>): Boolean {
        if (standings.isEmpty()) return false

        val backup = StandingsBackup(
            standings = standings.toList(),
            createdAtMillis = System.currentTimeMillis()
        )
        standingsStorage.saveManualBackup(backup)
        standingsBackup = backup
        return true
    }

    fun deleteBackup() {
        standingsStorage.deleteManualBackup()
        standingsBackup = null
    }
}