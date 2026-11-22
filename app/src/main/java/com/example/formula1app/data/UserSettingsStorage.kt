package com.example.formula1app.data

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.appearanceDataStore by preferencesDataStore(name = "f1_appearance")

data class AppearanceSettings(
    val darkTheme: Boolean?,
    val languageCode: String
)

class UserSettingsStorage(context: Context) {
    private val appContext = context.applicationContext
    private val profilePreferences by lazy {
        appContext.getSharedPreferences("f1_profile", Context.MODE_PRIVATE)
    }
    private val appearanceStore = appContext.appearanceDataStore

    val appearance = appearanceStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppearanceSettings(
                darkTheme = preferences[DARK_THEME],
                languageCode = preferences[LANGUAGE_CODE] ?: "ru"
            )
        }

    suspend fun readProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val login = profilePreferences.getString(LOGIN, null)
        val email = profilePreferences.getString(EMAIL, null)
        if (login == null || email == null) null else UserProfile(email, login)
    }

    suspend fun saveProfile(profile: UserProfile, newPassword: String?) {
        val credentials = newPassword?.let { password ->
            withContext(Dispatchers.Default) {
                val algorithm = if (Build.VERSION.SDK_INT >= 26) PBKDF2_SHA256 else PBKDF2_SHA1
                val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                PasswordCredentials(algorithm, salt, deriveHash(password, salt, algorithm))
            }
        }

        withContext(Dispatchers.IO) {
            val editor = profilePreferences.edit()
                .putString(LOGIN, profile.login)
                .putString(EMAIL, profile.email)

            if (credentials != null) {
                editor.putString(PASSWORD_ALGORITHM, credentials.algorithm)
                    .putString(PASSWORD_SALT, Base64.encodeToString(credentials.salt, Base64.NO_WRAP))
                    .putString(PASSWORD_HASH, Base64.encodeToString(credentials.hash, Base64.NO_WRAP))
            }

            if (!editor.commit()) throw IOException("Could not save the profile")
        }
    }

    suspend fun verifyPassword(password: String): Boolean {
        val credentials = withContext(Dispatchers.IO) {
            val algorithm = profilePreferences.getString(PASSWORD_ALGORITHM, null)
                ?: return@withContext null
            val salt = profilePreferences.getString(PASSWORD_SALT, null)
                ?: return@withContext null
            val hash = profilePreferences.getString(PASSWORD_HASH, null)
                ?: return@withContext null

            if (algorithm != PBKDF2_SHA256 && algorithm != PBKDF2_SHA1) {
                return@withContext null
            }

            try {
                val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
                val hashBytes = Base64.decode(hash, Base64.NO_WRAP)
                if (saltBytes.size != 16 || hashBytes.size != 32) return@withContext null
                PasswordCredentials(algorithm, saltBytes, hashBytes)
            } catch (_: IllegalArgumentException) {
                null
            }
        } ?: return false

        return withContext(Dispatchers.Default) {
            val enteredHash = deriveHash(password, credentials.salt, credentials.algorithm)
            MessageDigest.isEqual(enteredHash, credentials.hash)
        }
    }

    suspend fun setDarkTheme(darkTheme: Boolean) {
        appearanceStore.edit { it[DARK_THEME] = darkTheme }
    }

    suspend fun setLanguage(languageCode: String) {
        appearanceStore.edit { it[LANGUAGE_CODE] = languageCode }
    }

    private fun deriveHash(password: String, salt: ByteArray, algorithm: String): ByteArray {
        val iterations = if (algorithm == PBKDF2_SHA256) 600_000 else 1_400_000
        val passwordChars = password.toCharArray()
        val spec = PBEKeySpec(passwordChars, salt, iterations, 256)
        passwordChars.fill('\u0000')

        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private data class PasswordCredentials(
        val algorithm: String,
        val salt: ByteArray,
        val hash: ByteArray
    )

    private companion object {
        const val LOGIN = "login"
        const val EMAIL = "email"
        const val PASSWORD_ALGORITHM = "password_algorithm"
        const val PASSWORD_SALT = "password_salt"
        const val PASSWORD_HASH = "password_hash"
        const val PBKDF2_SHA256 = "PBKDF2WithHmacSHA256"
        const val PBKDF2_SHA1 = "PBKDF2WithHmacSHA1"

        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }
}