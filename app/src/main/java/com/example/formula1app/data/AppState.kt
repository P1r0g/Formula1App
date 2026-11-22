package com.example.formula1app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object AppState {
    private lateinit var standingsStorage: StandingsStorage
    private lateinit var settingsStorage: UserSettingsStorage
    private val profileMutex = Mutex()

    var settingsLoaded: Boolean by mutableStateOf(false)
        private set
    var userProfile: UserProfile? by mutableStateOf(null)
        private set
    var isSignedIn: Boolean by mutableStateOf(false)
    var darkTheme: Boolean? by mutableStateOf(null)
        private set
    var languageCode: String by mutableStateOf("ru")
        private set

    var standingsBackup: StandingsBackup? by mutableStateOf(null)
        private set

    fun initializeStorage(context: Context) {
        if (::standingsStorage.isInitialized) return

        settingsStorage = UserSettingsStorage(context)
        standingsStorage = StandingsStorage(context)
        standingsBackup = standingsStorage.readManualBackup()
    }

    suspend fun observeUserSettings() {
        if (!settingsLoaded) userProfile = settingsStorage.readProfile()

        settingsStorage.appearance.collect { appearance ->
            darkTheme = appearance.darkTheme
            languageCode = appearance.languageCode
            settingsLoaded = true
        }
    }

    suspend fun saveProfile(profile: UserProfile, newPassword: String?) {
        profileMutex.withLock {
            withContext(NonCancellable) {
                settingsStorage.saveProfile(profile, newPassword)
                userProfile = profile
            }
        }
    }

    suspend fun matchesCredentials(login: String, password: String): Boolean =
        profileMutex.withLock {
            userProfile?.login == login && settingsStorage.verifyPassword(password)
        }

    suspend fun updateDarkTheme(darkTheme: Boolean) = settingsStorage.setDarkTheme(darkTheme)

    suspend fun updateLanguage(languageCode: String) = settingsStorage.setLanguage(languageCode)

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