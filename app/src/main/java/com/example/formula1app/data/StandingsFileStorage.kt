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
import java.io.FileNotFoundException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class StandingsFileInfo(
    val path: String,
    val sizeBytes: Long,
    val modifiedAtMillis: Long
)

data class StandingsFileStatus(
    val file: StandingsFileInfo?,
    val hasBackup: Boolean
)

class StandingsFileStorage(context: Context) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val preferences = appContext.getSharedPreferences("f1_export", Context.MODE_PRIVATE)
    private val json = Json { prettyPrint = true }
    private val fileMutex = Mutex()
    private val internalBackup = AtomicFile(File(appContext.filesDir, FILE_NAME))

    suspend fun write(snapshot: StandingsBackup): Boolean = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            if (hasInternalBackup()) return@withLock false

            val text = json.encodeToString(StandingsBackup.serializer(), snapshot)
            writePublicFile(text.toByteArray(Charsets.UTF_8))
            true
        }
    }

    suspend fun readStatus(): StandingsFileStatus = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            StandingsFileStatus(
                file = readPublicFileInfo(),
                hasBackup = hasInternalBackup()
            )
        }
    }

    suspend fun deleteToBackup(): Unit = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = findDownload() ?: throw FileNotFoundException(FILE_NAME)
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IOException("Could not read the exported file")

                saveInternalBackup(bytes)
                if (resolver.delete(uri, null, null) != 1) {
                    throw IOException("Could not delete the exported file")
                }
                preferences.edit().remove(DOWNLOAD_URI).apply()
            } else {
                val file = legacyDownloadFile()
                saveInternalBackup(file.readBytes())
                if (!file.delete()) {
                    throw IOException("Could not delete the exported file")
                }
            }
        }
    }

    suspend fun restoreFromBackup(): Unit = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            writePublicFile(internalBackup.readFully())
            internalBackup.delete()
            if (hasInternalBackup()) {
                throw IOException("Could not remove the internal backup")
            }
        }
    }

    private fun hasInternalBackup(): Boolean = try {
        internalBackup.openRead().use { true }
    } catch (_: FileNotFoundException) {
        false
    }

    private fun saveInternalBackup(bytes: ByteArray) {
        writeAtomically(internalBackup, bytes)
        if (!internalBackup.readFully().contentEquals(bytes)) {
            throw IOException("Could not verify the internal backup")
        }
    }

    private fun writePublicFile(bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(bytes)
        } else {
            writeToLegacyDownloads(bytes)
        }
    }

    private fun readPublicFileInfo(): StandingsFileInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val file = legacyDownloadFile()
            return if (file.isFile) {
                StandingsFileInfo(
                    path = "${Environment.DIRECTORY_DOWNLOADS}/${file.name}",
                    sizeBytes = file.length(),
                    modifiedAtMillis = file.lastModified()
                )
            } else {
                null
            }
        }

        val uri = findDownload() ?: return null
        val columns = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        return resolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            StandingsFileInfo(
                path = cursor.getString(1) + cursor.getString(0),
                sizeBytes = cursor.getLong(2),
                modifiedAtMillis = cursor.getLong(3) * 1000L
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findDownload(): Uri? {
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        return findDownload(collection, "${Environment.DIRECTORY_DOWNLOADS}/")
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
    private fun legacyDownloadFile(): File {
        val directory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        return File(directory, FILE_NAME)
    }

    private fun writeToLegacyDownloads(bytes: ByteArray) {
        val file = legacyDownloadFile()
        val directory = file.parentFile ?: throw IOException("Downloads directory is missing")
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw IOException("Could not open the Downloads directory")
        }

        writeAtomically(AtomicFile(file), bytes)
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

    private companion object {
        const val FILE_NAME = "f1_standings_20.txt"
        const val DOWNLOAD_URI = "download_uri"
    }
}