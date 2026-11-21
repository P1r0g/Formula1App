package com.example.formula1app.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json

class StandingsStorage(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("f1_standings", Context.MODE_PRIVATE)

    fun readCache(): StandingsBackup? = read(CACHE_KEY)

    fun saveCache(snapshot: StandingsBackup) {
        write(CACHE_KEY, snapshot)
    }

    fun readManualBackup(): StandingsBackup? = read(BACKUP_KEY)

    fun saveManualBackup(snapshot: StandingsBackup) {
        write(BACKUP_KEY, snapshot)
    }

    fun deleteManualBackup() {
        preferences.edit().remove(BACKUP_KEY).apply()
    }

    private fun read(key: String): StandingsBackup? {
        return try {
            val json = preferences.getString(key, null)
            json?.let { Json.decodeFromString(StandingsBackup.serializer(), it) }
        } catch (error: IllegalArgumentException) {
            Log.w("F1Standings", "Could not read $key", error)
            null
        }
    }

    private fun write(key: String, snapshot: StandingsBackup) {
        preferences.edit()
            .putString(key, Json.encodeToString(StandingsBackup.serializer(), snapshot))
            .apply()
    }

    private companion object {
        const val CACHE_KEY = "last_loaded_standings"
        const val BACKUP_KEY = "manual_standings_backup"
    }
}