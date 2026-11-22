package com.example.formula1app.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.AtomicFile
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class StandingsFileStorage(context: Context) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val preferences = appContext.getSharedPreferences("f1_export", Context.MODE_PRIVATE)
    private val json = Json { prettyPrint = true }

    suspend fun write(snapshot: StandingsBackup) = withContext(Dispatchers.IO) {
        val text = json.encodeToString(StandingsBackup.serializer(), snapshot)
        val bytes = text.toByteArray(Charsets.UTF_8)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(bytes)
        } else {
            writeToLegacyDownloads(bytes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeToMediaStore(bytes: ByteArray) {
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val directory = "${Environment.DIRECTORY_DOWNLOADS}/"
        val existing = findDownload(collection, directory)

        if (existing != null) {
            val previous = resolver.openInputStream(existing)?.use { it.readBytes() }
                ?: throw IOException("Could not read the previous export")

            try {
                writeBytes(existing, bytes)
            } catch (error: IOException) {
                runCatching { writeBytes(existing, previous) }
                    .exceptionOrNull()?.let(error::addSuppressed)
                throw error
            }
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values)
            ?: throw IOException("Could not create the export in Downloads")

        try {
            writeBytes(uri, bytes)
            val published = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            if (resolver.update(uri, published, null, null) != 1) {
                throw IOException("Could not publish the export")
            }
            preferences.edit().putString(DOWNLOAD_URI, uri.toString()).apply()
        } catch (error: Exception) {
            runCatching { resolver.delete(uri, null, null) }
                .exceptionOrNull()?.let(error::addSuppressed)
            throw error
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findDownload(collection: Uri, directory: String): Uri? {
        val savedUri = preferences.getString(DOWNLOAD_URI, null)?.let(Uri::parse)
        val ownedDownload = "${MediaStore.MediaColumns.OWNER_PACKAGE_NAME} = ? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(appContext.packageName, directory)
        val projection = arrayOf(BaseColumns._ID)

        if (savedUri != null) {
            val exists = resolver.query(savedUri, projection, ownedDownload, selectionArgs, null)
                ?.use { it.moveToFirst() } == true
            if (exists) return savedUri
        }

        val selection = "$ownedDownload AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        return resolver.query(collection, projection, selection, selectionArgs + FILE_NAME, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    ContentUris.withAppendedId(collection, cursor.getLong(0))
                } else {
                    null
                }
            }
    }

    private fun writeBytes(uri: Uri, bytes: ByteArray) {
        val output = resolver.openOutputStream(uri, "wt")
            ?: throw IOException("Could not open the export for writing")
        output.use { it.write(bytes) }
    }

    @Suppress("DEPRECATION")
    private fun writeToLegacyDownloads(bytes: ByteArray) {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Could not open the Downloads directory")
        }

        val file = AtomicFile(File(directory, FILE_NAME))
        val output = file.startWrite()
        try {
            output.write(bytes)
            file.finishWrite(output)
        } catch (error: IOException) {
            file.failWrite(output)
            throw error
        }
    }

    private companion object {
        const val FILE_NAME = "f1_standings_20.txt"
        const val DOWNLOAD_URI = "download_uri"
    }
}