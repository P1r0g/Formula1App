package com.example.formula1app.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.File
import android.util.AtomicFile
import java.io.FileNotFoundException
import java.io.IOException


class StandingsStorage(context: Context) {
    private val filesDir = context.applicationContext.filesDir
    private val cacheFile = AtomicFile(File(filesDir, "standings_cache.json"))
    private val manualBackupFile = AtomicFile(File(filesDir, "standings_manual_backup.json"))

    fun readCache(): StandingsBackup? = read(cacheFile)

    fun deleteCache() {
        cacheFile.delete()
    }

    fun readManualBackup(): StandingsBackup? = read(manualBackupFile)

    fun saveManualBackup(snapshot: StandingsBackup) = write(manualBackupFile, snapshot)

    fun deleteManualBackup() {
        manualBackupFile.delete()
    }

    private fun read(file: AtomicFile): StandingsBackup? {
        return try {
            val json = file.readFully().decodeToString()
            Json.decodeFromString(StandingsBackup.serializer(), json)
        } catch (_: FileNotFoundException) {
            null
        } catch (error: IOException) {
            Log.w("F1Standings", "Could not read ${file.baseFile.name}", error)
            null
        } catch (error: IllegalArgumentException) {
            Log.w("F1Standings", "Invalid JSON in ${file.baseFile.name}", error)
            null
        }
    }

    private fun write(file: AtomicFile, snapshot: StandingsBackup) {
        val json = Json.encodeToString(StandingsBackup.serializer(), snapshot)
        writeAtomically(file, json.encodeToByteArray())
    }

    private fun writeAtomically(file: AtomicFile, bytes: ByteArray) {
        val output = file.startWrite()
        try {
            output.write(bytes)
            file.finishWrite(output)
        } catch (error: IOException) {
            file.failWrite(output)
            throw error
        }
    }
}