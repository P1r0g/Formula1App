package com.example.formula1app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {

    var userProfile: UserProfile? by mutableStateOf(null)

    var standings: List<DriverStanding> by mutableStateOf(
        DemoStandings.items
    )

    var darkTheme: Boolean? by mutableStateOf(null)

    var languageCode: String by mutableStateOf("ru")

    var standingsBackup: StandingsBackup? by mutableStateOf(null)
        private set

    fun createBackup(): Boolean {
        val currentStandings = standings

        if (currentStandings.isEmpty()) {
            return false
        }

        standingsBackup = StandingsBackup(
            standings = currentStandings.toList(),
            createdAtMillis = System.currentTimeMillis()
        )

        return true
    }

    fun restoreBackup(): Boolean {
        val backup = standingsBackup ?: return false

        standings = backup.standings.toList()
        return true
    }

    fun deleteBackup() {
        standingsBackup = null
    }
}